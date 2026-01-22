package com.jaxxnitt.myapplication.data.repository

import com.jaxxnitt.myapplication.data.database.Contact
import com.jaxxnitt.myapplication.data.database.ContactDao
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()

    suspend fun getAllContactsOnce(): List<Contact> {
        return contactDao.getAllContactsOnce()
    }

    suspend fun getContactById(id: Long): Contact? {
        return contactDao.getContactById(id)
    }

    suspend fun insert(contact: Contact): Long {
        return contactDao.insert(contact)
    }

    suspend fun update(contact: Contact) {
        contactDao.update(contact)
    }

    suspend fun delete(contact: Contact) {
        contactDao.delete(contact)
    }

    suspend fun deleteById(id: Long) {
        contactDao.deleteById(id)
    }
}
