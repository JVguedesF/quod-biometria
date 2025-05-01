#!/bin/bash

# Script para iniciar a aplicação QUOD Biometria em ambientes dev e prod

# Definir cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Função para imprimir mensagens de erro e sair
error_exit() {
    echo -e "${RED}ERRO: $1${NC}" 1>&2
    exit 1
}

# Função para imprimir mensagens de aviso
warning() {
    echo -e "${YELLOW}AVISO: $1${NC}"
}

# Função para imprimir mensagens de sucesso
success() {
    echo -e "${GREEN}$1${NC}"
}

# Verificar se o Docker e o Docker Compose estão instalados
if ! command -v docker &> /dev/null; then
    error_exit "Docker não está instalado. Por favor, instale o Docker antes de continuar."
fi

if ! command -v docker-compose &> /dev/null; then
    error_exit "Docker Compose não está instalado. Por favor, instale o Docker Compose antes de continuar."
fi

# Verificar o ambiente especificado
ENV="$1"
if [ -z "$ENV" ]; then
    warning "Nenhum ambiente especificado. Usando 'dev' como padrão."
    ENV="dev"
fi

# Validar o ambiente
case "$ENV" in
    dev|development)
        ENV_FILE=".env-dev"
        ENV_NAME="desenvolvimento"
        PROFILE="dev"
        APP_PORT=8090
        MONGO_PORT=27018
        JAVA_XMX="512m"
        ;;
    prod|production)
        ENV_FILE=".env-prod"
        ENV_NAME="produção"
        PROFILE="prod"
        APP_PORT=8080
        MONGO_PORT=27018
        JAVA_XMX="1g"
        ;;
    *)
        error_exit "Ambiente inválido: $ENV. Use 'dev' ou 'prod'."
        ;;
esac

# Verificar se o arquivo de ambiente existe
if [ ! -f "$ENV_FILE" ]; then
    error_exit "Arquivo de ambiente $ENV_FILE não encontrado. Por favor, crie o arquivo antes de continuar."
fi

# Verificar se o mongo-init.js existe
if [ ! -f "mongo-init.js" ]; then
    warning "Arquivo mongo-init.js não encontrado. Criando um arquivo padrão..."
    cat > mongo-init.js << 'EOL'
// Este script é executado quando o contêiner MongoDB é iniciado pela primeira vez

// Criar o usuário para a aplicação com permissões adequadas
db.createUser({
  user: process.env.MONGO_APP_USERNAME,
  pwd: process.env.MONGO_APP_PASSWORD,
  roles: [
    {
      role: "readWrite",
      db: process.env.MONGO_INITDB_DATABASE
    }
  ]
});

// Conectar ao banco de dados da aplicação
db = db.getSiblingDB(process.env.MONGO_INITDB_DATABASE);

// Criar coleções básicas
db.createCollection("users");
db.createCollection("biometric_templates");
db.createCollection("verification_attempts");

// Inserir um usuário administrador (opcional)
db.users.insertOne({
  username: "admin",
  password: "$2a$10$X7Vo9qYHnPo0Yk/oYdWpBe.3ZC1WO.wPQ8XD5fcwBM.1I0JXlyZ.u", // "admin" com bcrypt
  email: "admin@quod.com",
  roles: ["ADMIN"],
  active: true,
  createdAt: new Date()
});

print("Banco de dados inicializado com sucesso!");
EOL
    success "Arquivo mongo-init.js criado."
fi

# Copiar o arquivo de ambiente para .env e adicionar variáveis extras
cp "$ENV_FILE" .env || error_exit "Não foi possível copiar $ENV_FILE para .env"
echo "APP_PORT=$APP_PORT" >> .env
echo "MONGO_PORT=$MONGO_PORT" >> .env
echo "JAVA_XMX=$JAVA_XMX" >> .env
success "Configurações do ambiente de $ENV_NAME aplicadas."

# Verificar se existe algum contêiner em execução na porta 8080
if [ "$APP_PORT" -eq 8080 ] && lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    warning "A porta 8080 já está em uso. Verificando se é um contêiner Docker..."

    if docker ps | grep -q ":8080->"; then
        warning "Um contêiner Docker está usando a porta 8080. Tentando pará-lo..."
        docker stop $(docker ps | grep ":8080->" | awk '{print $1}')
    else
        warning "Outro processo está usando a porta 8080. Por favor, encerre-o manualmente."
        warning "Alternativamente, você pode usar uma porta diferente."
        read -p "Deseja especificar uma porta diferente? (S/n): " change_port
        if [[ "$change_port" =~ ^[Ss]$ ]]; then
            read -p "Nova porta para a aplicação: " APP_PORT
            sed -i "s/APP_PORT=.*/APP_PORT=$APP_PORT/" .env
            success "Porta alterada para $APP_PORT."
        else
            error_exit "Por favor, libere a porta 8080 e tente novamente."
        fi
    fi
fi

# Iniciar os containers
echo "Iniciando serviços QUOD Biometria no ambiente de $ENV_NAME..."
if [ "$ENV" == "dev" ] || [ "$ENV" == "development" ]; then
    # No ambiente de desenvolvimento, iniciar com o admin MongoDB Express
    docker-compose --profile dev up -d || error_exit "Falha ao iniciar os containers."
else
    # Ambiente de produção
    docker-compose up -d || error_exit "Falha ao iniciar os containers."
fi

# Verificar se os containers estão em execução
if docker-compose ps | grep -q "Up"; then
    success "Serviços QUOD Biometria iniciados com sucesso no ambiente de $ENV_NAME!"

    # Mostrar informações adicionais
    echo -e "\nServiços disponíveis:"
    echo "- API QUOD Biometria: http://localhost:$APP_PORT"

    if [ "$ENV" == "dev" ] || [ "$ENV" == "development" ]; then
        echo "- MongoDB Express (Admin): http://localhost:8081"
        echo "- API Docs: http://localhost:$APP_PORT/swagger-ui.html"
    fi

    echo -e "\nPara verificar o status dos serviços: docker-compose ps"
    echo "Para visualizar logs: docker-compose logs -f"
    echo "Para parar os serviços: docker-compose down"
else
    error_exit "Alguns serviços não iniciaram corretamente. Verifique os logs com 'docker-compose logs'."
fi

exit 0