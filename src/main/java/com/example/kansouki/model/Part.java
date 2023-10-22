package com.example.kansouki.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import lombok.Data;

@Data
public class Part {
  private String id;
  private String name;
  private List<Part> questions = new ArrayList<Part>();
  private long difficulty = -1;

  public Part(DocumentSnapshot snapshot, String sessionId) {
    id = snapshot.getId();
    name = snapshot.getString("name");
    Object difficultyObject = snapshot.get("difficulty");
    if(difficultyObject != null){
      try {
        var difficultyMap = (Map<String, Object>) difficultyObject;
        if(difficultyMap.get(sessionId) != null){
          this.difficulty = (long)difficultyMap.get(sessionId);
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  public void load(Firestore db, String sessionId) {
    ApiFuture<QuerySnapshot> query = db.collection("Parts").whereEqualTo("parent", id).get();
    try {
      QuerySnapshot querySnapshot = query.get();
      List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
      for (QueryDocumentSnapshot document : documents) {
        Part part = new Part(document, sessionId);
        part.load(db, sessionId);
        questions.add(part);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
