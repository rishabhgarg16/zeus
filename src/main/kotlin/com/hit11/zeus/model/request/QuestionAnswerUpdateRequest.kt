package com.hit11.zeus.model.request

import com.hit11.zeus.model.PulseResult

class QuestionAnswerUpdateRequest(
    var pulseId: Int = 0,
    var pulseResult: PulseResult = PulseResult.UNDECIDED,
)