package com.google.codelabs.mdc.kotlin.shrine.core.data

import com.google.codelabs.mdc.kotlin.shrine.core.database.PaymentMethodDao
import com.google.codelabs.mdc.kotlin.shrine.core.model.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Per-user masked payment methods (figma checkout + profile). No real card data is stored. */
interface PaymentRepository {
    fun paymentMethods(userId: Long): Flow<List<PaymentMethodEntity>>
    suspend fun getDefault(userId: Long): PaymentMethodEntity?
    suspend fun add(method: PaymentMethodEntity): Long
    suspend fun delete(method: PaymentMethodEntity)
    suspend fun setDefault(userId: Long, methodId: Long)
}

@Singleton
class DefaultPaymentRepository @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao,
) : PaymentRepository {

    override fun paymentMethods(userId: Long): Flow<List<PaymentMethodEntity>> =
        paymentMethodDao.observeByUser(userId)

    override suspend fun getDefault(userId: Long): PaymentMethodEntity? = paymentMethodDao.getDefault(userId)

    override suspend fun add(method: PaymentMethodEntity): Long {
        if (method.isDefault) paymentMethodDao.clearDefault(method.userId)
        return paymentMethodDao.insert(method)
    }

    override suspend fun delete(method: PaymentMethodEntity) = paymentMethodDao.delete(method)

    override suspend fun setDefault(userId: Long, methodId: Long) {
        paymentMethodDao.clearDefault(userId)
        paymentMethodDao.markDefault(methodId)
    }
}
