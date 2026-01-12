// MongoDB Initialization Script
// This script runs when the MongoDB container is first created

// Switch to server database
db = db.getSiblingDB('server');

// Create application user
db.createUser({
    user: 'server_app',
    pwd: 'server_password',
    roles: [
        { role: 'readWrite', db: 'server' }
    ]
});

// Create indexes for chat messages collection
db.createCollection('chat_messages');
db.chat_messages.createIndex({ "roomId": 1, "createdAt": -1 });
db.chat_messages.createIndex({ "senderId": 1 });
db.chat_messages.createIndex({ "createdAt": 1 }, { expireAfterSeconds: 2592000 }); // TTL: 30 days

// Create indexes for audit logs collection
db.createCollection('audit_logs');
db.audit_logs.createIndex({ "timestamp": -1 });
db.audit_logs.createIndex({ "userId": 1, "timestamp": -1 });
db.audit_logs.createIndex({ "action": 1 });
db.audit_logs.createIndex({ "createdAt": 1 }, { expireAfterSeconds: 7776000 }); // TTL: 90 days

print('MongoDB initialized successfully');
