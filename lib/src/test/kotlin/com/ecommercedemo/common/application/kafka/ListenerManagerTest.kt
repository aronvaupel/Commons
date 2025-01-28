package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.cache.keys.KafkaTopicRegistry
import com.ecommercedemo.common.application.kafka.handling.MainEventHandler
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.MessageListenerContainer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListenerManagerTest {

    private lateinit var redisService: RedisService
    private lateinit var repositoryScanner: RepositoryScanner
    private lateinit var kafkaListenerContainerFactory: ConcurrentKafkaListenerContainerFactory<String, Any>
    private lateinit var mainEventHandler: MainEventHandler<BaseEntity>
    private lateinit var listenerManager: ListenerManager<BaseEntity>

    @BeforeEach
    fun setUp() {
        redisService = mock(RedisService::class.java)
        repositoryScanner = mock(RepositoryScanner::class.java)
        kafkaListenerContainerFactory =
            mock(ConcurrentKafkaListenerContainerFactory::class.java) as ConcurrentKafkaListenerContainerFactory<String, Any>

        val mockListenerContainer =
            mock(ConcurrentMessageListenerContainer::class.java) as ConcurrentMessageListenerContainer<String, Any>
        `when`(kafkaListenerContainerFactory.createContainer(anyString())).thenReturn(mockListenerContainer)

        val mockConsumerFactory = mock(ConsumerFactory::class.java) as ConsumerFactory<String, Any>
        val mockConsumer =
            mock(org.apache.kafka.clients.consumer.Consumer::class.java) as org.apache.kafka.clients.consumer.Consumer<String, Any>
        `when`(mockConsumerFactory.createConsumer()).thenReturn(mockConsumer)
        `when`(kafkaListenerContainerFactory.consumerFactory).thenReturn(mockConsumerFactory)

        mainEventHandler = mock(MainEventHandler::class.java) as MainEventHandler<BaseEntity>

        listenerManager = ListenerManager(
            redisService,
            repositoryScanner,
            kafkaListenerContainerFactory,
            mainEventHandler,
            "test-service"
        )
    }

    @Test
    fun `init should initialize downstream entities`() {
        val downstreamEntities = listOf("Entity1", "Entity2")
        val kafkaTopics = KafkaTopicRegistry(
            topics = mutableMapOf(
                "entity1" to null,
                "entity2" to null
            )
        )
        `when`(repositoryScanner.getDownstreamEntityNames()).thenReturn(downstreamEntities)
        `when`(redisService.getKafkaRegistry()).thenReturn(kafkaTopics)

        listenerManager.init()

        verify(repositoryScanner).getDownstreamEntityNames()
        assertEquals(downstreamEntities, listenerManager.downstreamEntities)
    }

    @Test
    fun `stopKafkaListener should stop and remove the listener`() {
        val topic = "testTopic"
        val mockListenerContainer = mock(MessageListenerContainer::class.java)
        listenerManager.listenerContainers[topic] = mockListenerContainer

        listenerManager.stopKafkaListener(topic)

        verify(mockListenerContainer).stop()
        assertTrue(!listenerManager.listenerContainers.containsKey(topic))
    }
}
