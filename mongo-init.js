// MongoDB initialization script
db = db.getSiblingDB(process.env.MONGO_DATABASE);

// Create user for application with proper authentication
db.createUser({
    user: process.env.MONGO_APP_USERNAME,
    pwd: process.env.MONGO_APP_PASSWORD,
    roles: [
        { role: 'readWrite', db: process.env.MONGO_DATABASE }
    ]
});

// Create collections
db.createCollection('users');
db.createCollection('biometric_data');
db.createCollection('documents');
db.createCollection('fraud_notifications');
db.createCollection('feature_flags');
db.createCollection('model_metadata');
db.createCollection('ab_test_results');
db.createCollection('device_contexts');

// Create indexes for users collection
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });

// Create indexes for biometric_data collection
db.biometric_data.createIndex({ "transactionId": 1 });
db.biometric_data.createIndex({ "userId": 1 });
db.biometric_data.createIndex({ "status": 1 });
db.biometric_data.createIndex({ "type": 1 });
db.biometric_data.createIndex({ "fraudDetected": 1 });
db.biometric_data.createIndex({ "capturedAt": 1 });
db.biometric_data.createIndex({ "processedAt": 1 });
db.biometric_data.createIndex({ "confidenceScore": 1 });

// Create indexes for documents collection
db.documents.createIndex({ "transactionId": 1 });
db.documents.createIndex({ "userId": 1 });
db.documents.createIndex({ "status": 1 });
db.documents.createIndex({ "type": 1 });
db.documents.createIndex({ "fraudDetected": 1 });
db.documents.createIndex({ "capturedAt": 1 });
db.documents.createIndex({ "processedAt": 1 });

// Create indexes for fraud_notifications collection
db.fraud_notifications.createIndex({ "transactionId": 1 });
db.fraud_notifications.createIndex({ "relatedEntityId": 1 });
db.fraud_notifications.createIndex({ "status": 1 });
db.fraud_notifications.createIndex({ "detectedAt": 1 });
db.fraud_notifications.createIndex({ "notifiedAt": 1 });
db.fraud_notifications.createIndex({ "fraudType": 1 });

// Create indexes for feature_flags collection
db.feature_flags.createIndex({ "name": 1 }, { unique: true });

// Create indexes for model_metadata collection
db.model_metadata.createIndex({ "modelName": 1 });
db.model_metadata.createIndex({ "version": 1 });
db.model_metadata.createIndex({ "activeFrom": 1 });

// Create indexes for ab_test_results collection
db.ab_test_results.createIndex({ "testId": 1 });
db.ab_test_results.createIndex({ "transactionId": 1 });
db.ab_test_results.createIndex({ "testGroup": 1 });

// Insert initial feature flags
db.feature_flags.insertMany([
    {
        name: "enhanced_face_detection",
        globallyEnabled: false,
        rolloutPercentage: 0,
        enabledForUsers: [],
        description: "Enhanced face detection algorithm using deep learning",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        name: "deepfake_detection_v2",
        globallyEnabled: false,
        rolloutPercentage: 0,
        enabledForUsers: [],
        description: "New version of deepfake detection algorithm",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        name: "adaptive_device_processing",
        globallyEnabled: true,
        rolloutPercentage: 100,
        enabledForUsers: [],
        description: "Detect device capabilities and adapt processing accordingly",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

const adminUser = {
    username: "admin",
    // Não mais usamos uma senha hardcoded - será gerada durante o setup inicial
    passwordHash: "$2a$10$" + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
    fullName: "Admin User",
    email: "admin@quod.com",
    roles: ["ADMIN"],
    mustChangePassword: true,
    createdAt: new Date(),
    updatedAt: new Date()
};

db.users.insertOne(adminUser);

// Mostrar a senha inicial temporária do admin apenas na primeira inicialização
print("==================================================================");
print("IMPORTANTE: Uma conta de administrador foi criada");
print("Username: admin");
print("Para definir a senha inicial do admin, execute o comando:");
print("docker exec -it quod-app java -jar /app.jar --set-admin-password");
print("==================================================================");

// Insert initial model metadata
db.model_metadata.insertMany([
    {
        modelName: "facenet_face_detection",
        version: "1.0.0",
        description: "Initial face detection model",
        accuracy: 0.95,
        activeFrom: new Date(),
        parameters: {
            confidenceThreshold: 0.7,
            minFaceSize: 64,
            scaleFactor: 1.1
        },
        createdAt: new Date()
    },
    {
        modelName: "deepfake_detector",
        version: "1.0.0",
        description: "Initial deepfake detection model",
        accuracy: 0.92,
        activeFrom: new Date(),
        parameters: {
            confidenceThreshold: 0.65,
            featureExtractorType: "efficientnet",
            temporalWindowSize: 16
        },
        createdAt: new Date()
    }
]);