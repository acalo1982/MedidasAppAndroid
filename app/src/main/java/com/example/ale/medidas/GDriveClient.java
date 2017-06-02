package com.example.ale.medidas;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by ale on 02/06/2017.
 */

public class GDriveClient {

    private GoogleApiClient apiClient;//cliente para manejar la conexión con Google Drive
    private String LOGTAG = "GDriveClient";
    private String strFolderId;//folder "MedidasApp" la cuenta de Drive de "micromag@micromag.es" creado a mano
    private String strFileId;
    private OnMessageReceived mMessageListener = null; //interfaz para devolver la comunicación a la clase que llame a ésta
    private boolean operacionOk;
    private String strFileIdnew;

    public GDriveClient(GoogleApiClient client,String strFolderId,String strFileId,OnMessageReceived listener) {
        apiClient = client;
        mMessageListener = listener; //servirá para una vez acabada la tarea de esta clase, notificar a la clase llamante
        this.strFileId=strFileId;//ID del archivo de sobre el que queremos operar (copiar o borrar a/de GDrive)
        this.strFolderId=strFolderId;//Directorio donde se encuentran esos archivos
    }


    //GDrive: Crear Archivo dendro de directorio conocido
    public void createFile(final String filename) {
        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (result.getStatus().isSuccess()) {
                            writeSampleText(result.getDriveContents());
                            MetadataChangeSet changeSet =
                                    new MetadataChangeSet.Builder()
                                            .setTitle(filename)
                                            .setMimeType("text/plain")
                                            .build();
                            //Opción 1: Directorio raíz
                            //DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);
                            //Opción 2: Otra carpeta distinta al directorio raiz
                            DriveFolder folder = DriveId.decodeFromString(strFolderId).asDriveFolder();
                            folder.createFile(apiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(DriveFolder.DriveFileResult result) {
                                            if (result.getStatus().isSuccess()) {
                                                operacionOk=true;
                                                strFileIdnew=result.getDriveFile().getDriveId().toString();
                                                Log.i(LOGTAG, "Fichero creado con ID = " + result.getDriveFile().getDriveId());
                                            } else {
                                                operacionOk=false;
                                                strFileIdnew="";
                                                Log.e(LOGTAG, "Error al crear el fichero");
                                            }
                                        }
                                    });
                        } else {
                            operacionOk=false;
                            strFileIdnew="";
                            Log.e(LOGTAG, "Error al crear DriveContents");
                        }
                    }
                });
    }

    //GDrive: Escribe el contenido del fichero al objeto de su contenido
    private void writeSampleText(DriveContents driveContents) {
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        try {
            writer.write("Esto es un texto de prueba!");
            writer.close();
        } catch (IOException e) {
            Log.e("writeSampleText", "Error al escribir en el fichero: " + e.getMessage());
        }
    }

    //GDrive: Lo usamos para borrar los archivos de medida: Tendríamos que guardar el FolderID dentro del XML junto a las medidas, para poder borrarlo
    public void deleteFile() {
        //DriveFile file = fileDriveId.asDriveFile();
        DriveFile file = DriveId.decodeFromString(strFileId).asDriveFile();//recuperamos el ID a partir del String

        //Opción 1: Enviar a la papelera
        file.trash(apiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess())
                    Log.i(LOGTAG, "Fichero eliminado correctamente.");
                else
                    Log.e(LOGTAG, "Error al eliminar el fichero");
            }
        });
        //Opción 2: Eliminar
        //file.delete(apiClient).setResultCallback(...)
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
