function clickedLogin() {
  const provider = new firebase.auth.GoogleAuthProvider();
  firebase.auth().signInWithPopup(provider).then().catch((error) => {
      alert(`fail to sign in. ${error.message}`)
  });
}

function clickedLogout() {
  firebase.auth().signOut().then();
}