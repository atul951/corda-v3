package com.template.server

import com.template.AtulIssueRequest
import com.template.AtulState
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import net.corda.core.messaging.startFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import net.corda.core.messaging.vaultQueryBy
import java.time.LocalDateTime
import net.corda.core.utilities.toBase64
import javax.enterprise.inject.Produces
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc



private const val CONTROLLER_NAME = "config.controller.name"

/**
 *  A controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/api/template")
private class restController(private val rpc: NodeRPCConnection,
        @Value("\${config.rpc.port}") val rpcPort: Int,
        @Value("\${$CONTROLLER_NAME}") private val controllerName:String
    ){

    companion object {
        private val logger = LoggerFactory.getLogger(restController::class.java)
    }

    private val myName = rpc.proxy.nodeInfo().legalIdentities.first().name

    @GetMapping("/date", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getCurrentDate(): Any{
        return mapOf("date" to LocalDateTime.now().toLocalDate())
    }

    @GetMapping("/port", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getPort(): String{
        return rpcPort.toString()
    }

    /**
     *  Request asset issuance
     */
    @PostMapping("'issue-asset-request", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun issueAssetRequest(@RequestBody params: IssueParams): ResponseEntity<String>{
        val issuerX500 = params.issuer
        return try {
            val proxy = rpc.proxy
            val issuerID = proxy.wellKnownPartyFromX500Name(issuerX500) ?: throw IllegalArgumentException("Could not find issuer node '$issuerX500'.")
            proxy.startFlow(::AtulIssueRequest, params.thought, issuerID).returnValue.getOrThrow()
            logger.info("Issue Request completed successfully: $params.thought")
            ResponseEntity.status(HttpStatus.CREATED).build()
        }catch(e: Exception){
            logger.error("Issue Request Failed", e)
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @GetMapping("/myname", produces = ["application/plain"])
    private fun muName() = myName.toString()

    @GetMapping("peers", produces = ["application/json"])
    private fun peersName(): Map<String, List<String>>{
        val nodes = rpc.proxy.networkMapSnapshot()
        val nodesNames = nodes.map {it.legalIdentities.first().name}
        val filteredNodeName = nodesNames.filter { it.organisation !in listOf(controllerName, myName) }
        val filteredNodeNamesToString = filteredNodeName.map { it.toString() }
        return mapOf("peers" to filteredNodeNamesToString)
    }

    @GetMapping("/getatuls", produces = ["application/json"])
    private fun getAtuls(): List<Map<String, Any>> {
        val atulStateAndRef = rpc.proxy.vaultQueryBy<AtulState>().states
        val atulState = atulStateAndRef.map { it }
        return atulState.map { mapOf(
                "issuer" to it.state.data.issuer.toString(),
                "owner" to it.state.data.owner.toString(),
                "thought" to it.state.data.thought,
                "hash" to it.ref.txhash.bytes.toBase64(),
                "index" to it.ref.index
        ) }
    }

}

@Configuration
//@EnableWebMvc
@ComponentScan
class WebConfig : WebMvcConfigurerAdapter() {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry?) {
        registry!!.addResourceHandler("/**").addResourceLocations("/")
    }
}



private data class IssueParams(val thought: String, val issuer: CordaX500Name)
