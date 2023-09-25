import firebase from "firebase/compat/app";
import "firebase/compat/database";
import express from "express";
import bodyParser from "body-parser";

const app = express();
app.use(bodyParser.json());

const firebaseConfig = {
  apiKey: 'AIzaSyCcoPloKRnZr0txC2vyc4h-NdMePGChIIs',
  projectId : 'secquraise-ceb6d',
  appId: '1:80845131896:android:e0b78695c465000d619d8d',
  databaseURL: 'https://secquraise-ceb6d-default-rtdb.asia-southeast1.firebasedatabase.app'
};
firebase.initializeApp(firebaseConfig);

const db = firebase.database().ref('/user_details');

app.post('/capture', (req, res) => {
  const data = req.body;

  db.push().set(data, (error) => {
    if (error) {
      console.error('Error storing data:', error);
      return res.status(500).json({ error: 'Error storing data' });
    }
    return res.status(200).json({ message: 'Data stored successfully' });
  });
});

setInterval(() => {

  const capturedData = {
    internetConnectivity: 'connected', 
    batteryStatus: 'charging', 
    batteryPercentage: 80, 
    location: '12.34, 56.78',
    timestamp: new Date().toISOString() 
  };

  db.push().set(capturedData, (error) => {
    if (error) {
      console.error('Error storing data:', error);
    }
  });
}, 15 * 60 * 1000); 

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
