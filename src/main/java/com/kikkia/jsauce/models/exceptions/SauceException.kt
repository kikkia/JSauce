package com.kikkia.jsauce.models.exceptions

import java.lang.Exception

class SauceException(message: String, exception: Exception? = null) : Exception(message, exception)