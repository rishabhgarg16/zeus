package com.hit11.zeus.controller

import com.hit11.zeus.model.ApiResponse
import com.hit11.zeus.service.MatchService
import com.sun.org.apache.xpath.internal.operations.Bool
import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.util.HtmlUtils
import java.io.IOException


@Controller
class MatchWSController(private val matchService: MatchService) {


    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    @Throws(Exception::class)
    fun greeting(message: HelloMessage): Greeting {
        return Greeting("Hello, " + HtmlUtils.htmlEscape(message.name) + "!")
    }

    @MessageMapping("/match/upcoming")
    @SendTo("/topic/greetings")
    @Throws(Exception::class)
    fun upcomingMatchesWS(message: HelloMessage): Greeting {
        val data = matchService.getUpcomingMatches(4)
        return Greeting("Hello, " + HtmlUtils.htmlEscape(message.name) + "!")
    }

    @RequestMapping("/sse")
    @ResponseBody
    fun handle(): SseEmitter {
        val emitter = SseEmitter()
        Thread(Runnable {
            while (true) {
                try {
                    val data = matchService.getUpcomingMatches(4)
                    val response = ApiResponse(
                        status = HttpStatus.OK.value(),
                        internalCode = null,
                        message = "Success",
                        data = data
                    )
                    emitter.send(SseEmitter.event().name("message").data(response))
                    Thread.sleep(1000)
                } catch (e: IOException) {
                    emitter.completeWithError(e)
                    return@Runnable
                } catch (e: InterruptedException) {
                    emitter.completeWithError(e)
                    return@Runnable
                }
            }
        }).start()
        return emitter
    }

    @RequestMapping("/reload")
    @ResponseBody
    fun reload(): Boolean {
        return true
    }

}

class HelloMessage(val name: String = "")

class Greeting(val content: String)
