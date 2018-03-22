package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function
import com.typesafe.config.ConfigFactory
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TimeWindow
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.serialization.SerializationWhitelist
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.security.PublicKey
import java.time.Duration
import java.time.Instant
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response




val CORP_NAME = CordaX500Name(organisation = "BCS Learning", locality = "Sydney", country = "AU")
internal val NOTARY_NAME = CordaX500Name(organisation =  "Turicum Notary Service", locality = "Zurich", country = "CH")
internal val BOD_NAME = CordaX500Name(organisation = "Bank of Atul", locality = "Delhi", country = "IN")
private var whitelistedIssuers: Set<CordaX500Name> = emptySet()

// *****************
// * API Endpoints *
// *****************
@Path("template")
class ExampleApi(val rpcOps: CordaRPCOps) {
    // Accessible at /api/template/templateGetEndpoint.
    @GET
    @Path("templateGetEndpoint")
    @Produces(MediaType.APPLICATION_JSON)
    fun templateGetEndpoint(): Response {
        return Response.ok("Template GET endpoint.").build()
    }
}


private fun getIssuerWhitelist(serviceHub: ServiceHub): Set<PublicKey> {
    if (whitelistedIssuers.isEmpty()) {
        val tempSet: MutableSet<CordaX500Name> = mutableSetOf()
        val conf = ConfigFactory.parseResources("application.conf")
        conf.getObjectList("whitelists.atul_issuers").flatMapTo(tempSet, { x ->
            val cn = x.get("common_name")
            val parsedName: CordaX500Name = when (cn) {
                null -> CordaX500Name(
                        x.get("organization")?.unwrapped() as String? ?: "",
                        x.get("locality")?.unwrapped() as String? ?: "",
                        x.get("country")?.unwrapped() as String? ?: ""
                )
                else -> CordaX500Name(
                        cn.unwrapped() as String? ?: "",
                        x.get("organization")?.unwrapped() as String? ?: "",
                        x.get("locality")?.unwrapped() as String? ?: "",
                        x.get("country")?.unwrapped() as String? ?: ""
                )
            }
            listOf(parsedName)
        })
        whitelistedIssuers = tempSet
    }
    val outs: MutableSet<PublicKey> = mutableSetOf()
    whitelistedIssuers.flatMapTo(outs, { x ->
        val key = serviceHub.identityService.wellKnownPartyFromX500Name(x)?.owningKey
        if (key != null) {
            listOf(key)
        } else {
            emptyList()
        }
    })
    return outs
}


// *********
// * Flows *
// *********

@InitiatingFlow
@StartableByRPC
class AtulIssueRequest(val thought: String, val issuer: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.getNotary(NOTARY_NAME) ?: throw FlowException("Could not find the trusted Turicum Notary node.")
      //  logger.info(notary.toString())
        val selfID = serviceHub.myInfo.legalIdentities[0]
      //  logger.info(selfID.toString())
        val issueTxBuilder = AtulContract.generateIssue(thought, issuer, selfID, notary)

        val bankSession = initiateFlow(issuer)

        issueTxBuilder.setTimeWindow(TimeWindow.fromStartAndDuration(Instant.now(serviceHub.clock), Duration.ofMillis(10000)))

        // Verifying the transaction.
        issueTxBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(issueTxBuilder)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(bankSession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        return subFlow(FinalityFlow(fullySignedTx))
    }
}

@InitiatedBy(AtulIssueRequest::class)
class AtulIssueResponse(val counterpartySession: FlowSession) : FlowLogic<Unit>() { @Suspendable override fun call() { val signTransactionFlow = object : SignTransactionFlow(counterpartySession, SignTransactionFlow.tracker()) {
    override fun checkTransaction(stx: SignedTransaction) = requireThat {
        val whitelistedIssuers = getIssuerWhitelist(serviceHub)
        val output = stx.tx.outputs.single().data
        "This must be a Atul transaction." using (output is AtulState)
        val atul = output as AtulState
        "I must be a whitelisted node" using (whitelistedIssuers.contains(ourIdentity.owningKey))
        "The Atul must be issued by a whitelisted node" using (whitelistedIssuers.contains(atul.issuer.owningKey))
        "The issuer of a Atul must be the issuing node" using (atul.issuer.owningKey == ourIdentity.owningKey)
    }
}

    subFlow(signTransactionFlow)
}
}
/*
@InitiatingFlow
@StartableByRPC
class AtulMoveRequest(val atul: StateAndRef<AtulState>, val newOwner: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.getNotary(NOTARY_NAME) ?: throw FlowException("Could not find Turicum Notary node.")

        val txBuilder = TransactionBuilder(notary=notary)
        AtulContract.generateMove(txBuilder, atul, newOwner)

        val moveSession = initiateFlow(newOwner)

        txBuilder.setTimeWindow(TimeWindow.fromStartAndDuration(Instant.now(serviceHub.clock), Duration.ofMillis(10000)))

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(moveSession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        return subFlow(FinalityFlow(fullySignedTx))
    }
}

@InitiatedBy(AtulMoveRequest::class)
class AtulMoveResponse(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val whitelistedIssuers = getIssuerWhitelist(serviceHub)
                val output = stx.tx.outputs.single().data
                "This must be a Atul transaction." using (output is AtulState)
                val atul = output as AtulState
                "The Atul must be issued by a whitelisted node" using (whitelistedIssuers.contains(atul.issuer.owningKey))
                "The issuer of a Atul must be the issuing node" using (atul.issuer.owningKey == ourIdentity.owningKey)
            }
        }

        subFlow(signTransactionFlow)
    }
}

*/

// Serialization whitelist.
class TemplateSerializationWhitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(TransactionBuilder::class.java)
}


// This class is not annotated with @CordaSerializable, so it must be added to the serialization whitelist, above, if
// we want to send it to other nodes within a flow.
data class TemplateData(val payload: String)
