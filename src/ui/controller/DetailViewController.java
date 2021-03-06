package ui.controller;

import connect.Playlist;
import connect.Song;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import persistence.DataManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class DetailViewController implements Initializable {
   @FXML
   private TableView<Song> tableView;
   @FXML
   private TableColumn<Song, String> title, artist, album, length;

   @FXML
   private ImageView image;
   @FXML
   private Label name, type;

   private ObservableList<Song> songObservableList;

   private PanelType panelType;

   private RightViewController parentViewController;

   /**
    * Called to initialize a controller after its root element has been
    * completely processed.
    *
    * @param location  The location used to resolve relative paths for the root object, or
    *                  <tt>null</tt> if the location is not known.
    * @param resources The resources used to localize the root object, or <tt>null</tt> if
    */
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      songObservableList = FXCollections.observableArrayList();
      title.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTitle()));
      artist.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getArtist()));
      album.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAlbumTitle()));
      length.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getReadableDuration()));
      tableView.setRowFactory(tv -> {
         TableRow<Song> row = new TableRow<>();
         row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getClickCount() == 2) {
               Song clickedRow = row.getItem();
               if(event.getButton() == MouseButton.PRIMARY){
                  mouseClicked(clickedRow);
               }

            }
         });
         row.setContextMenu(new SongsContextMenu(row.getItem(),(ObservableList<Playlist>)parentViewController.getCurrentLibrary().getPlaylists()));
         return row;
      });
      tableView.setItems(songObservableList);
   }


   /**
    * This function will be called when a song is clicked on. It will play that song.
    *
    * @param song clicked song.
    */
   public void mouseClicked(Song song) {
      parentViewController.addSongToQueue(song);
   }

   /**
    * Set the type of parent panel this view is in.
    *
    * @param panelType panel type
    */
   public void setPanelType(PanelType panelType) {

      this.panelType = panelType;
   }

   /**
    * This function will be called when "addAllToQueue" button is clicked. It will add all songs to queue.
    */
   public void addAllToQueue() {
      for (Song song : songObservableList) {
         parentViewController.addSongToQueue(song);
      }
   }

   /**
    * This function will be call when "Close" button is clicked. It called the parent view controller to destroy this view.
    */
   public void close() {
      parentViewController.removeDetailView(panelType);
   }

   /**
    * Set the parent controller of this view
    *
    * @param rightViewController parent controller.
    */
   public void setParentViewController(RightViewController rightViewController) {

      parentViewController = rightViewController;
   }

   /**
    * Use this function to provide complete information for detailView
    *
    * @param imagePath          path to an image file.
    * @param name               name of this thing
    * @param type               type of this thing
    * @param songObservableList songs in this thing
    */
   public void updateDetailView(String imagePath, String name, String type, ObservableList<Song> songObservableList) {
      if (imagePath != null) {
         image.setImage(new Image(imagePath));
      }
      this.name.setText(name);
      this.type.setText(type);
      this.songObservableList = songObservableList;
      tableView.setItems(songObservableList);
   }

   /**
    * Get the current list of songs
    *
    * @return list of songs
    */
   public ObservableList<Song> getSongObservableList() {
      return songObservableList;
   }

   /**
    * Set a new list of songs
    *
    * @param songObservableList a new list of songs.
    */
   public void setSongObservableList(ObservableList<Song> songObservableList) {
      this.songObservableList = songObservableList;
      tableView.setItems(songObservableList);
   }

}
