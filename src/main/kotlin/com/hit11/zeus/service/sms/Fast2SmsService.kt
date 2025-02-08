package com.hit11.zeus.service.sms

import com.hit11.zeus.ZeusApplication
import com.hit11.zeus.repository.UserRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Fast2SmsService(
    private  val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(Fast2SmsService::class.java)
    private val FAST_2_SMS_API_ENDPOINT = ""
    private val FAST_2_SMS_API_KEY = "IGxHcL0WOJBUyhpokqYaXASNs8jrwzZ9vQdCE374gK5unT2VtfgmGsebfMpXrFZ0IqzNvwxWo7KnS64P"

    val otp = 786598

    fun sendOtp(mobileNumber: String): String {
        var stripedMobile = mobileNumber.substring(3)
        userRepository.findByPhone(mobileNumber)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://www.fast2sms.com/dev/bulkV2?" +
                    "authorization=$FAST_2_SMS_API_KEY" +
                    "&route=otp" +
                    "&variables_values=$otp" +
                    "&flash=1" +
                    "&numbers=$stripedMobile")
            .build()
        val response = client.newCall(request).execute()
        logger.info("Response: ${response.body?.string()}")
        if (response.isSuccessful) {
            return otp.toString()    
        }
        throw OtpGenerationException()
    }

    fun verifyOtp(mobileNumber: String, otp: String): Boolean {
        return true
    }
}

class OtpGenerationException : Throwable() {

}
