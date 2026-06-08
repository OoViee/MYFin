package com.example

import com.example.data.TripExpenseEntity
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSimpleEqualSplit() {
        // Alice pays 100 for Alice and Bob
        val participants = listOf("Alice", "Bob")
        val expenses = listOf(
            TripExpenseEntity(
                id = 1,
                tripId = 1,
                title = "Dinner",
                totalAmount = 100.0,
                paidBy = "Alice",
                splitMethod = "EQUAL",
                participantWeights = "",
                involvedParticipants = "Alice, Bob",
                category = "Food"
            )
        )

        val settlements = calculateSimplifiedSettlements(participants, expenses)
        
        assertEquals(1, settlements.size)
        val transaction = settlements[0]
        assertEquals("Bob", transaction.debtor)
        assertEquals("Alice", transaction.creditor)
        assertEquals(50.0, transaction.amount, 0.01)
    }

    @Test
    fun testTransitiveDebtSimplification() {
        // Alice pays 100 for Alice and Bob (Bob owes Alice 50)
        // Bob pays 100 for Bob and Charlie (Charlie owes Bob 50)
        val participants = listOf("Alice", "Bob", "Charlie")
        val expenses = listOf(
            TripExpenseEntity(
                id = 1,
                tripId = 1,
                title = "Dinner",
                totalAmount = 100.0,
                paidBy = "Alice",
                splitMethod = "EQUAL",
                participantWeights = "",
                involvedParticipants = "Alice, Bob",
                category = "Food"
            ),
            TripExpenseEntity(
                id = 2,
                tripId = 1,
                title = "Taxi",
                totalAmount = 100.0,
                paidBy = "Bob",
                splitMethod = "EQUAL",
                participantWeights = "",
                involvedParticipants = "Bob, Charlie",
                category = "Transport"
            )
        )

        val settlements = calculateSimplifiedSettlements(participants, expenses)
        
        // Simplified settlement should have Charlie owe Alice 50 directly, and Bob has 0 net balance so 0 transactions.
        assertEquals(1, settlements.size)
        val transaction = settlements[0]
        assertEquals("Charlie", transaction.debtor)
        assertEquals("Alice", transaction.creditor)
        assertEquals(50.0, transaction.amount, 0.01)
    }
}
