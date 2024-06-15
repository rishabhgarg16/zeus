//package com.hit11.zeus.service
////
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        remoteMessage.notification?.let {
//            sendNotification(it.body)
//        }
//    }
//
//    private fun sendNotification(messageBody: String?) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//
//        val notificationBuilder = NotificationCompat.Builder(this, "default")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("FCM Message")
//            .setContentText(messageBody)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(0, notificationBuilder.build())
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        // Send the new token to your server
//    }
//}
