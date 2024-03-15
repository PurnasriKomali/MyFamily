package com.purnasri.myfamily

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private val listContacts:ArrayList<ContactModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listMembers = listOf(
            MemberModel("Lucy", "9th building 2nd floor, Maldiv road, Manali 9th building", "90%", "220"),
            MemberModel("Honey", "10th building 2nd floor, Maldiv road, Manali 10th building", "100%", "223"),
            MemberModel("Puri", "11th building 2nd floor, Maldiv road, Manali 11th building", "80%", "210"),
            MemberModel("hello", "12th building 2nd floor, Maldiv road, Manali 12th building", "60%", "200")
        )

        val adapter = MemberAdapter(listMembers)
        val recycler = requireView().findViewById<RecyclerView>(R.id.recycler_member)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter



        Log.d("FetchContact89", "fetchContacts: starting")

        Log.d("FetchContact89", "fetchContacts: starting")
        val inviteAdapter = InviteAdapter(listContacts)

        Log.d("FetchContact89", "fetchContacts: ending")

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("FetchContact89", "fetchContacts: coroutine start")


            fetchDatabaseContacts()

            listContacts.addAll( fetchContacts())
            insertDatabaseContacts(listContacts)
            withContext(Dispatchers.Main){
                inviteAdapter.notifyDataSetChanged()
            }

            Log.d("FetchContact89", "fetchContacts: coroutine end ${listContacts.size}")
        }

        val inviteRecycler = requireView().findViewById<RecyclerView>(R.id.recycler_invite)
        inviteRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        inviteRecycler.adapter = inviteAdapter
    }

    private fun fetchDatabaseContacts() : LiveData<List<ContactModel?>?>? {
            val database = MyFamilyDatabase.getDatabase(requireContext())
            return database.contactDao().allContacts


    }


    private suspend fun insertDatabaseContacts(listContacts: ArrayList<ContactModel>) {

        val database = MyFamilyDatabase.getDatabase(requireContext())

        database.contactDao().insertAll(listContacts)

    }

    private fun fetchContacts(): ArrayList<ContactModel> {

        Log.d("FetchContact89", "fetchContacts: start")

        val cr = requireActivity().contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        val listContacts: ArrayList<ContactModel> = ArrayList()


        if (cursor != null && cursor.count > 0) {

            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.run { getColumnIndex(ContactsContract.Contacts._ID) })
                val name =
                    cursor.getString(cursor.run { getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) })
                val hasPhoneNumber =
                    cursor.getInt(cursor.run { getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER) })

                if (hasPhoneNumber > 0) {

                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        ""
                    )

                    if (pCur != null && pCur.count > 0) {

                        while (pCur != null && pCur.moveToNext()) {

                            val phoneNum =
                                pCur.getString(pCur.run { getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) })

                            listContacts.add(ContactModel(name, phoneNum))

                        }
                        pCur.close()

                    }

                }
            }

            if (cursor != null) {
                cursor.close()
            }

        }

        Log.d("FetchContact89", "fetchContacts: end")
        return listContacts

    }
    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
