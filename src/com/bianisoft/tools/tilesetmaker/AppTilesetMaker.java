package com.bianisoft.tools.tilesetmaker;


//Standard Java library imports
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;


public class AppTilesetMaker{
	String			m_stFilenameIn;
	String			m_stFilenameOut;
	BufferedImage	m_imgSource;
	int				m_nImgWidth;
	int				m_nImgHeight;
	int				m_nTileSize;
	int				m_nNbTileX;
	int				m_nNbTileY;
	int				m_nNbTileUnique;

	BufferedImage[][]	m_arTiles;
	Map<String, BufferedImage> m_mapTiles= new ConcurrentHashMap<String, BufferedImage>();

	BufferedImage	m_imgDestination;


	public AppTilesetMaker(String p_stFilename, int p_nTileSize) throws IOException{
		System.out.print("\nCreating App obj with" + p_stFilename + Integer.toString(p_nTileSize));

		m_imgSource= ImageIO.read(new File(m_stFilenameIn= p_stFilename));
		m_nImgWidth= m_imgSource.getWidth();
		m_nImgHeight= m_imgSource.getHeight();
		m_nTileSize= p_nTileSize;
		m_nNbTileX= m_nImgWidth / m_nTileSize;
		m_nNbTileY= m_nImgHeight / m_nTileSize;
		m_stFilenameOut= m_stFilenameIn.substring(0, m_stFilenameIn.indexOf(".png")) + "_out.png";

		System.out.print("\nFile Details: " + Integer.toString(m_nImgWidth));
		System.out.print("\n\tDimension: " + Integer.toString(m_nImgWidth) + " X " + Integer.toString(m_nImgHeight));
		System.out.print("\n\tDimension in Tiles: " + Integer.toString(m_nNbTileX) + " X " + Integer.toString(m_nNbTileY));
	}

	public void doSplit(){
		System.out.print("\n\nStarting Splitting of the file):\n");
		m_arTiles= new BufferedImage[m_nNbTileX][m_nNbTileY];

		for(int i= 0; i < m_nNbTileX; ++i){
			for(int j= 0; j < m_nNbTileY; ++j){
				m_arTiles[i][j]= m_imgSource.getSubimage((i * m_nTileSize), (j * m_nTileSize), m_nTileSize, m_nTileSize);
				System.out.print(".");	System.out.flush();
			}
		}
		System.out.print("Done!");
	}

	public String returnHex(byte[] inBytes) throws Exception{
		String hexString= "";

		for(int i= 0; i < inBytes.length; ++i)
			hexString+= Integer.toString((inBytes[i] & 0xff) + 0x100, 16).substring(1);

		return hexString;
	}

	public void doMD5AndMap() throws Exception{
		System.out.print("\n\nStarting MD5 Claculation and Mapping\n");

		for(int i= 0; i < m_nNbTileX; ++i){
			for(int j= 0; j < m_nNbTileY; ++j){
				ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
				ImageIO.write(m_arTiles[i][j], "png", outputStream);

				byte[] data= outputStream.toByteArray();

				MessageDigest md= MessageDigest.getInstance("MD5");
				md.update(data);
				byte[] hash= md.digest();
				String stHash= returnHex(hash);

				m_mapTiles.put(stHash, m_arTiles[i][j]);
				System.out.print(".");	System.out.flush();
			}
		}

		m_nNbTileUnique= m_mapTiles.size();
		System.out.print("Done!\nNb Unique Tiles: " + Integer.toString(m_nNbTileUnique));
	}

	public void doMerge(){
		System.out.print("\n\nStarting Merge\n");

		Collection<BufferedImage>	m_vBufTiles= m_mapTiles.values();
		int i= 0;

		m_imgDestination= new BufferedImage(m_nNbTileUnique * m_nTileSize, m_nTileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g= m_imgDestination.createGraphics();

		for(BufferedImage bufImage : m_vBufTiles){
			g.drawImage(bufImage, null, i * m_nTileSize, 0);
			++i;
			System.out.print(".");	System.out.flush();
		}

		System.out.print("Done!");
	}

	public void doSavePNG() throws IOException{
		System.out.print("\n\nSaving Result PNG as" + m_stFilenameOut + "\n");
		ImageIO.write(m_imgDestination, "png", new File(m_stFilenameOut));
	}

    public static void main(String[] args) {
		String	stFilename;
		int		nTileSize;

		if(args.length < 2){
			JFileChooser fc= new JFileChooser(".");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG file", "png");

			fc.setFileFilter(filter);
			if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return;

			stFilename	= fc.getSelectedFile().getAbsolutePath();
			nTileSize	= Integer.parseInt(JOptionPane.showInputDialog("What is the tiles size? "));
		}else{
			stFilename	= args[0];
			nTileSize	= Integer.parseInt(args[1]);
		}

		try{
			AppTilesetMaker app= new AppTilesetMaker(stFilename, nTileSize);
			app.doSplit();
			app.doMD5AndMap();
			app.doMerge();
			app.doSavePNG();
		}catch(Exception e){
			System.out.print(e);
		}
	}
}
