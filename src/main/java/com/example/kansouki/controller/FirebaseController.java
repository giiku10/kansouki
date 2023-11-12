package com.example.kansouki.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.kansouki.model.ClassObject;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;

@Controller
public class FirebaseController {

  @Autowired
  private HttpSession session;
  private Firestore db;
  private FirebaseAuth auth;

  String encryptionKey = System.getenv("ENCRYPTION_KEY");

  public FirebaseController() {
    try {
      // FileInputStream refreshToken = new
      // FileInputStream("/home/ubuntu/kansou-ki.json");
      FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
          .setCredentials(GoogleCredentials.getApplicationDefault()).build();
      db = firestoreOptions.getService();
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.getApplicationDefault())
          .build();
      FirebaseApp app = FirebaseApp.initializeApp(options);
      auth = FirebaseAuth.getInstance(app);
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
  public ClassObject sendClass(@NonNull String classId) {
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
  public String sendData(@RequestParam @NonNull String partId, @RequestParam Integer value) {
    DocumentReference partRef = db.collection("Parts").document(partId);
    Map<String, Map<String, Integer>> difficultyData = new HashMap<>();
    Map<String, Integer> data = new HashMap<>();
    data.put(session.getId(), value);
    difficultyData.put("difficulty", data);
    partRef.set(difficultyData, SetOptions.merge());
    return "test";
  }

  @PostMapping("isEditableClass")
  @ResponseBody
  public @Nullable String isEditableClass(@RequestParam @NonNull String classId, @RequestParam String token){
    System.out.println(classId);
    String uid = getUserId(token);
    if (uid == null) {
      return null;
    }

    ApiFuture<DocumentSnapshot> future = db.collection("Class").document(encrypt(classId)).get();
    try {
      DocumentSnapshot snapshot = future.get();
      String classUid = snapshot.getString("userId");
      if (classUid == null) {
        return null;
      }
      if (classUid.equals(uid)) {
        return encrypt(uid);
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  @PostMapping("isEditablePart")
  @ResponseBody
  public Boolean isEditablePart(@RequestParam @NonNull String partId, @RequestParam String token) {
    String uid = getUserId(token);
    if (uid == null) {
      return false;
    }

    ApiFuture<DocumentSnapshot> futurePart = db.collection("Parts").document(encrypt(partId)).get();
    try {
      DocumentSnapshot snapshotPart = futurePart.get();
      String classId = snapshotPart.getString("classId");
      if (classId == null) {
        return false;
      }
      ApiFuture<DocumentSnapshot> futureClass = db.collection("Class").document(classId).get();
      try {
        DocumentSnapshot snapshotClass = futureClass.get();
        String classUid = snapshotClass.getString("userId");
        if (classUid == null) {
          return false;
        }
        return classId.equals(classUid);
      } catch (Exception e) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  @PostMapping("/getUserId")
  @ResponseBody
  public @Nullable String getUserId(@RequestParam String token) {
    System.out.println(encrypt(token));
    try {
      FirebaseToken decodedToken = auth.verifyIdToken(encrypt(token));
      String uid = decodedToken.getUid();
      System.out.println(uid);
      return encrypt(uid);
    } catch (Exception e) {
      System.err.println("error getting uid");
      return null;
    }
  }

  private @NonNull String encrypt(@NonNull String text) {
    String[] textArray = text.split("");
    String[] encryptKeyArray = encryptionKey.split("");
    String returnValue = "";
    for (int i = 0; i < textArray.length; i++) {
      returnValue += encryptUnit(text.codePointAt(i), encryptKeyArray[i % 2000]);
    }
    return returnValue;
  }

  private @NonNull String encryptUnit(@NonNull Integer value, @NonNull String key) {
    Integer intKey = Integer.parseInt(key);
    var outputValue = (value + intKey + 61) % 94 + 33;
    return Character.toString(outputValue);
  }
}