const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.notifyCustomerOnStatusChange = functions.firestore
  .document("ShopkeeperRequests/{shopkeeperId}/Requests/{requestId}")
  .onUpdate(async (change, context) => {
    const after = change.after.data();
    const before = change.before.data();

    if (after.status === before.status) return null; // Only trigger if status changes

    const customerId = after.customerId;
    const itemName = after.itemName || "your order";
    const status = after.status;

    let messageText = "";
    if (status === "Accepted") {
      messageText = `✅ Your order for "${itemName}" has been accepted!`;
    } else if (status === "Cancelled") {
      messageText = `❌ Your order for "${itemName}" has been cancelled.`;
    } else {
      messageText = `ℹ️ Your order for "${itemName}" is now ${status}.`;
    }

    // Save to customer Firestore
    await admin.firestore()
      .collection("CustomerNotifications")
      .doc(customerId)
      .collection("Notifications")
      .add({
        message: messageText,
        status: status,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });

    // Get customer's FCM token
    const tokenDoc = await admin.firestore().collection("UserTokens").doc(customerId).get();
    const fcmToken = tokenDoc.exists ? tokenDoc.data().token : null;

    if (fcmToken) {
      const payload = {
        notification: {
          title: "Order Update",
          body: messageText,
          click_action: "FLUTTER_NOTIFICATION_CLICK" // For compatibility
        }
      };

      await admin.messaging().sendToDevice(fcmToken, payload);
      console.log(`📩 Notification sent to ${customerId}`);
    } else {
      console.log(`⚠️ No FCM token found for customer ${customerId}`);
    }

    return null;
  });
