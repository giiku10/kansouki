package com.example.kansouki.model;

import java.util.ArrayList;
import java.util.List;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import lombok.Data;

@Data
public class ClassObject {
  private String id;
  private String name;
  private List<Part> questions = new ArrayList<Part>();

  public ClassObject(){}
  
  public ClassObject(DocumentSnapshot snapshot){
    id = snapshot.getId();
    name = snapshot.getString("name");
  }

  public void load(Firestore db, String sessionId){
    ApiFuture<QuerySnapshot> query = db.collection("Parts").whereEqualTo("classId", id).whereEqualTo("parent", "").get();
    try{
      QuerySnapshot querySnapshot = query.get();
      List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
      for (QueryDocumentSnapshot document : documents) {
        Part part = new Part(document);
        part.load(db, sessionId);
        questions.add(part);
      }
    }catch(Exception e){
      System.out.println(e);
    }
  }
}
