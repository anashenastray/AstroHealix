package com.example.astrohealix.evaluator

import com.example.astrohealix.model.BodySystemsCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyInterlockEngineTest {

    @Test
    fun cprSuggestion_enforcesBulkheadCounterResistance_whenOmitted() {
        val result = SafetyInterlockEngine.validateAndEnforce(
            rawSuggestion = "Initiate CPR chest compressions immediately.",
            cabinCo2MmHg = 2.1f,
            heartRateBpm = 0f,
            cabinO2Percent = 20.9f
        )

        assertTrue(result.rulesVerified.any { it.contains("Microgravity") })
        assertTrue(result.modifiedText.contains("Hand-Stand") || result.modifiedText.contains("Reverse-Bearhug"))
        assertTrue(result.modifiedText.contains("bulkhead"))
    }

    @Test
    fun flammabilityCeiling_triggersSafetyHalt_whenO2ExceedsLimit() {
        val result = SafetyInterlockEngine.validateAndEnforce(
            rawSuggestion = "Increase cabin oxygen flow to enrich breathing atmosphere.",
            cabinCo2MmHg = 2.0f,
            heartRateBpm = 75f,
            cabinO2Percent = 25.5f
        )

        assertTrue(result.rulesVerified.any { it.contains("Flammability") })
        assertTrue(result.warnings.isNotEmpty())
        assertTrue(result.modifiedText.contains("24.1%"))
    }

    @Test
    fun simultaneousHypercapniaAndTachycardia_triggersInterlockPriority() {
        val result = SafetyInterlockEngine.validateAndEnforce(
            rawSuggestion = "Crew member reports severe headache and rapid breathing.",
            cabinCo2MmHg = 3.6f,
            heartRateBpm = 112f,
            cabinO2Percent = 21.0f
        )

        assertTrue(result.rulesVerified.any { it.contains("Hypercapnia-Tachycardia") })
        assertTrue(result.modifiedText.contains("Amine scrubbers"))
        assertTrue(result.modifiedText.contains("4-7-8"))
    }

    @Test
    fun excessiveEpiDose_isCappedAtOneMg() {
        val result = SafetyInterlockEngine.validateAndEnforce(
            rawSuggestion = "Administer high dose Epinephrine 2mg IV bolus now.",
            cabinCo2MmHg = 2.0f,
            heartRateBpm = 0f,
            cabinO2Percent = 20.9f
        )

        assertTrue(result.rulesVerified.any { it.contains("Drug Ceiling") })
        assertTrue(result.modifiedText.contains("1.0 mg"))
    }

    @Test
    fun clinicalProtocolsCatalog_containsDocumentMandatedProtocols() {
        val protocols = BodySystemsCatalog.getDefaultClinicalProtocols()
        val codes = protocols.map { it.code }

        assertTrue(codes.contains("NASA-HYPERCAP-TACHY"))
        assertTrue(codes.contains("NASA-ACLS-ZGRAV"))
        assertTrue(codes.contains("NASA-SAFE-INTERLOCK"))
        assertTrue(codes.contains("NASA-ENV-WANNINKHOF"))
    }
}
