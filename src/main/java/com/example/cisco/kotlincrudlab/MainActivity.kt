package com.example.cisco.kotlincrudlab


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.example.cisco.kotlincrudlab.controller.ItemRowListener
import com.example.cisco.kotlincrudlab.model.ToDoItem
import com.example.cisco.kotlincrudlab.controller.ToDoItemAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), ItemRowListener {

    lateinit var mDatabase: DatabaseReference
    var toDoItemList: MutableList<ToDoItem>? = null
    lateinit var adapter: ToDoItemAdapter
    var fbAuth = FirebaseAuth.getInstance()
    private var listViewItems: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        var btnLogOut = findViewById<Button>(R.id.btn_sign_out)
        listViewItems = findViewById<View>(R.id.items_list) as ListView

        fab.setOnClickListener { view ->

            addNewItemDialog()

        }
        btnLogOut.setOnClickListener{ view ->
            showMessage(view, "Logging Out...")
            signOut()
        }
        fbAuth.addAuthStateListener {
            if(fbAuth.currentUser == null){
                this.finish()
            }
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        toDoItemList = mutableListOf<ToDoItem>()
        adapter = ToDoItemAdapter(this, toDoItemList!!)
        listViewItems!!.setAdapter(adapter)
        mDatabase.orderByKey().addListenerForSingleValueEvent(itemListener)
    }

    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            addDataToList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {

            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }
    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)

        val itemEditText = EditText(this)
        alert.setMessage("Add New Item")
        alert.setTitle("Enter To Do Item Text")

        alert.setView(itemEditText)

        alert.setPositiveButton("Submit") { dialog, positiveButton ->
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText.text.toString()
            todoItem.done = false

            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
            todoItem.objectId = newItem.key

            newItem.setValue(todoItem)
            dialog.dismiss()
            Toast.makeText(this, "Item saved with ID " + todoItem.objectId, Toast.LENGTH_SHORT).show()
        }

        alert.show()
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()

        if (items.hasNext()) {
            val toDoListindex = items.next()
            val itemsIterator = toDoListindex.children.iterator()


            while (itemsIterator.hasNext()) {

                val currentItem = itemsIterator.next()
                val todoItem = ToDoItem.create()

                val map = currentItem.getValue() as HashMap<String, Any>

                todoItem.objectId = currentItem.key
                todoItem.done = map.get("done") as Boolean?
                todoItem.itemText = map.get("itemText") as String?
                toDoItemList!!.add(todoItem);
            }
        }

        adapter.notifyDataSetChanged()
    }
    fun signOut(){
        fbAuth.signOut()

    }

    fun showMessage(view: View, message: String){
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show()
    }

override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
    val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
    itemReference.child("done").setValue(isDone);
}

override fun onItemDelete(itemObjectId: String) {
    //get child reference in database via the ObjectID
    val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
    //deletion can be done via removeValue() method
    itemReference.removeValue()
}

}