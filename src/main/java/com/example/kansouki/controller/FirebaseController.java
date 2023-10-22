package com.example.kansouki.controller;

//import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.kansouki.model.ClassObject;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.SetOptions;

import jakarta.servlet.http.HttpSession;

@Controller
public class FirebaseController {

  @Autowired
  private HttpSession session;
  private Firestore db;

  public FirebaseController() {
    try {
      //FileInputStream refreshToken = new FileInputStream("/home/ubuntu/kansou-ki.json");
      FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
          .setCredentials(GoogleCredentials.getApplicationDefault()).build();
      db = firestoreOptions.getService();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @GetMapping("/")
  public String showMainPage(Model model) {
    return "index";
  }

  @PostMapping("/class")
  @ResponseBody
  public ClassObject sendClass(String classId) {
    ClassObject classObject = new ClassObject();
    ApiFuture<DocumentSnapshot> future = db.collection("Class").document(classId).get();
    try {
      DocumentSnapshot snapshot = future.get();
      classObject = new ClassObject(snapshot);
      classObject.load(db, session.getId());
    } catch (Exception e) {
      System.out.println(e);
    }
    return classObject;
  }

  @PostMapping("/sendValue")
  @ResponseBody
  public String sendData(String partId, Integer value) {
    DocumentReference partRef = db.collection("Parts").document(partId);
    Map<String, Map<String, Integer>> difficultyData = new HashMap<>();
    Map<String, Integer> data = new HashMap<>();
    data.put(session.getId(), value);
    difficultyData.put("difficulty", data);
    partRef.set(difficultyData, SetOptions.merge());
    return "test";
  }
}