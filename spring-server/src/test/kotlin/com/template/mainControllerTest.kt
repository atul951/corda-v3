package com.template

import com.template.server.DemoController
import com.template.server.NodeRPCConnection
import com.template.server.restController
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.omg.CORBA.Object
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity.status
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.lang.reflect.Array.get
import java.util.*
import javax.inject.Inject
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class mainControllerTest{

//    @InjectMocks
//    lateinit var restController: restController



    private val logger = LoggerFactory.getLogger(javaClass.name)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this);
    }

    @InjectMocks
    lateinit var demo : DemoController

    @Test
    @Throws(Exception::class)
    fun test(){
        var res = demo.getCurrentDate()
        assertNotNull(res)
    }

    @InjectMocks
    lateinit var rest : restController
    @Test
    @Throws(Exception::class)
    fun testRest(){
        var resp = rest.peersName()
        assertNotNull(resp)
    }


   @Test
    fun trueTest(){
       assertNull(null)
   }


}
