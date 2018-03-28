package com.template

import com.template.server.NodeRPCConnection
import com.template.server.restController
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.omg.CORBA.Object
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity.status
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.client.RestTemplate
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.context.WebApplicationContext
import java.lang.reflect.Array.get
import java.util.*
import javax.inject.Inject

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders.*
import javax.servlet.ServletContext
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.getForObject
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

private const val CONTROLLER_NAME = "config.controller.name"


@RunWith(SpringJUnit4ClassRunner::class)
class mainControllerTest{

  //  @Autowired
    private val rpc: NodeRPCConnection = NodeRPCConnection("localhost", "user1", "test", 10006)
    private val rpcPort: Int = 10006
    private val controllerName: String = CONTROLLER_NAME

    lateinit var mockMvc: MockMvc

    @InjectMocks
    val rest: restController =  restController(rpc, rpcPort, controllerName)


    @Before
    fun setup() {
       mockMvc = MockMvcBuilders.standaloneSetup(rest).build()
    }

    @Test
    fun dataTest(){

        mockMvc.perform(get("/api/template/mycurrency"))

                .andExpect(status().isOk)

      // currency = this.restTemplate.getForObject("/api/template/mycurrency", Currency::class)

    }


}
