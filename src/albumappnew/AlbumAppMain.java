/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package albumappnew;

/**
 *
 * @author tararamanan
 */
public class AlbumAppMain {

    public static void main(String[] args) {
        final AlbumAppManager albumAppManager = new AlbumAppManager();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                albumAppManager.createAndShowGUI();

            }
        });
    }

}
