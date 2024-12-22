package com.ecommercedemo.common.service.concretion

//@Aspect
//@Component
//class CacheAspect(
//    private val redisTemplate: RedisTemplate<String, String>,
//    private val redisService: RedisService,
//    private val objectMapper: ObjectMapper
//) {
//
//    @Around("@annotation(com.ecommercedemo.common.service.CachingEligible)")
//    fun cacheMethodResult(joinPoint: ProceedingJoinPoint): Any? {
//        val methodName = joinPoint.signature.name
//        val args = joinPoint.args
//
//        val key = redisService.generateMethodCacheKey(methodName, args)
//
//        val cachedResult = redisTemplate.opsForValue().get(key)
//        if (cachedResult != null) {
//            return objectMapper.readValue(cachedResult, joinPoint.signature.declaringType)
//        }
//
//        val result = joinPoint.proceed()
//
//        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result))
//
//        return result
//    }
//}
