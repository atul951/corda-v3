package com.template

import com.template.server.restController
import org.hamcrest.CoreMatchers.containsString
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.omg.CORBA.Object
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity.status
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.lang.reflect.Array.get
import java.util.*

@RunWith(SpringRunner::class)
@WebMvcTest(value = mainControllerTest::class, secure = false)
class mainControllerTest{

    @InjectMocks
    lateinit var control: restController

    @Autowired
    private val mockMvc: MockMvc? = null

    @Test
    fun dataTest(){
        val result = control.getcur()
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World")));
    }

}
