package com.google.codelabs.mdc.kotlin.shrine.core.data

import com.google.codelabs.mdc.kotlin.shrine.core.database.AddressDao
import com.google.codelabs.mdc.kotlin.shrine.core.model.AddressEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Per-user shipping addresses (figma checkout + profile). */
interface AddressRepository {
    fun addresses(userId: Long): Flow<List<AddressEntity>>
    suspend fun getDefault(userId: Long): AddressEntity?
    suspend fun add(address: AddressEntity): Long
    suspend fun update(address: AddressEntity)
    suspend fun delete(address: AddressEntity)
    suspend fun setDefault(userId: Long, addressId: Long)
}

@Singleton
class DefaultAddressRepository @Inject constructor(
    private val addressDao: AddressDao,
) : AddressRepository {

    override fun addresses(userId: Long): Flow<List<AddressEntity>> = addressDao.observeByUser(userId)

    override suspend fun getDefault(userId: Long): AddressEntity? = addressDao.getDefault(userId)

    override suspend fun add(address: AddressEntity): Long {
        if (address.isDefault) addressDao.clearDefault(address.userId)
        return addressDao.insert(address)
    }

    override suspend fun update(address: AddressEntity) {
        if (address.isDefault) addressDao.clearDefault(address.userId)
        addressDao.update(address)
    }

    override suspend fun delete(address: AddressEntity) = addressDao.delete(address)

    override suspend fun setDefault(userId: Long, addressId: Long) {
        addressDao.clearDefault(userId)
        addressDao.markDefault(addressId)
    }
}
