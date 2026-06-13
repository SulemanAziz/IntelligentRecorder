package com.intelligentrecorder.userInterface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning

@Composable
fun StopRecordingButton(){
    Button(
        onClick = {},
        modifier = Modifier
            .padding(vertical = 30.dp)
            .size(65.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        )
    ){

    }
}
@Composable
fun RecordButton(){
    IconButton(
        onClick = {},
        modifier = Modifier
            .padding(vertical = 30.dp)
            .size(75.dp),
    ) {
        Box(
            modifier = Modifier
                .size(100.dp) // The actual red circle size
                .clip(CircleShape)
                .background(Color.Red)
        )
    }
}

@Composable
fun SettingsMenu(){
    IconButton(
        onClick = {},
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            tint = Color.DarkGray,
            contentDescription = "Adjust Motion Detection Settings",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Composable
fun DeleteVideoBufferButton(){
    IconButton(
        //Flush the video buffer and start over.
        onClick = {},
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            tint = Color.Red,
            contentDescription = "Adjust Motion Detection Settings",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Composable
fun SaveButton(){
    IconButton(
        //Write to Video Output here from videoBuffer
        onClick = {},
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            tint = Color.Green,
            contentDescription = "Adjust Motion Detection Settings",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Composable
fun MainScreen(isRecording: Boolean, motion: Boolean){

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
    ) {
        if(motion){
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Motion State",
                modifier = Modifier.size(80.dp),
                tint = Color.Yellow
            )
        }
        else{
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Motion State",
                modifier = Modifier.size(80.dp),
                tint = Color.DarkGray
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(isRecording){
            StopRecordingButton()
        }
        else{
            RecordButton()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        SaveButton()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start
    ) {
        SettingsMenu()
        Spacer(Modifier.padding(vertical = 15.dp))
        DeleteVideoBufferButton()
    }


}

//Variables here will come from the ViewModel later
var isRecording:Boolean = false
var motion:Boolean = false

@Preview(showBackground = true)
@Composable
fun ScreenPreview(){
    MainScreen(isRecording, motion)
}