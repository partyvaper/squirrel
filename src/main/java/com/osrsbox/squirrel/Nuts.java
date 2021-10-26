package com.osrsbox.squirrel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.cache.*;
import net.runelite.cache.definitions.*;
import net.runelite.cache.definitions.exporters.NpcExporter;
import net.runelite.cache.definitions.loaders.WorldMapLoader;
import net.runelite.cache.fs.*;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import net.runelite.cache.definitions.loaders.ModelLoader;
import net.runelite.cache.fs.flat.FlatStorage;
import net.runelite.cache.models.ObjExporter;

public class Nuts
{
	public static void main(String[] args) throws IOException
	{
		Options options = new Options();

		options.addOption("h", "help", false, "Print this help menu");
		options.addOption("v", "version", false, "Print the version number");
		
		options.addOption("c", "cache", false, "Load cache from specific target folder");
		options.addOption("f", "flatcache", false, "Load flat cache from specific target folder");
		
		options.addOption("i", "items", false, "Dump ItemDefinitions in JSON to specific target folder");
		options.addOption("n", "npcs", false, "Dump NpcDefinitions in JSON to specific target folder");
		options.addOption("o", "objects", false, "Dump ObjectDefinitions in JSON to specific target folder");
		options.addOption("m", "models", false, "Dump models in OBJ + MTL to specific target folder");
		options.addOption("s", "sprites", false, "Dump sprites to specific target folder");
		options.addOption("x", "interfaces", false, "Dump interfaces to specific target folder");
		options.addOption("y", "mapdata", false, "Dump map data to specific target folder");
		options.addOption("z", "mapimages", false, "Dump map images to specific target folder");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException ex)
		{
			System.err.println("Error parsing command line options: " + ex.getMessage());
			System.exit(-1);
			return;
		}

		if (cmd.hasOption("help")) 
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "squirrel" , options );
			return;
		}

		if (cmd.hasOption("version")) 
		{
			String version = Nuts.class.getPackage().getImplementationVersion();
			System.out.println("squirrel - " + version);

			String runeliteCacheVersion = Nuts.class.getPackage().getImplementationTitle();
			System.out.println("runelite - " + runeliteCacheVersion);
			return;
		}

		// Load cache store
		Store store;
		if (cmd.hasOption("cache"))
		{
			String cache = cmd.getOptionValue("cache");
			if (cache == null)
			{
				cache = "cache";
			}
			System.out.println("[*] Loading cache from: " + cache);
			store = loadStore(cache);
		}
		else if (cmd.hasOption("flatcache"))
		{
			String cache = cmd.getOptionValue("flatcache");
			if (cache == null)
			{
				cache = "osrs-cache";
			}
			System.out.println("[*] Loading flatcache from: " + cache);
			store = loadStoreFlat(cache);
		}
		else
		{
			String cache = "osrs-cache";
			System.out.println("[*] Loading default flatcache from: " + cache);
			store = loadStoreFlat(cache);
		}

		// Try do something
		if (cmd.hasOption("items"))
		{
			String itemdir;
			itemdir = cmd.getOptionValue("items");

			if (itemdir == null)
			{
				itemdir = "dumps/items";
			}

			System.out.println("[*] Dumping items to: " + itemdir);
			dumpItems(store, new File(itemdir));
		}
		else if (cmd.hasOption("npcs"))
		{
			String npcdir;
			npcdir = cmd.getOptionValue("npcs");

			if (npcdir == null)
			{
				npcdir = "dumps/npcs";
			}

			System.out.println("[*] Dumping npcs to: " + npcdir);
			dumpNpcs(store, new File(npcdir));
		}
		else if (cmd.hasOption("objects"))
		{
			String objectdir;
			objectdir = cmd.getOptionValue("objects");

			if (objectdir == null)
			{
				objectdir = "dumps/objects";
			}

			System.out.println("[*] Dumping objects to: " + objectdir);
			dumpObjects(store, new File(objectdir));
		}
		else if (cmd.hasOption("models"))
		{
			String modeldir;
			modeldir = cmd.getOptionValue("models");

			if (modeldir == null)
			{
				modeldir = "dumps/models";
			}

			System.out.println("[*] Dumping models to: " + modeldir);
			dumpModels(store, new File(modeldir));
		} else if (cmd.hasOption("sprites"))
		{
			String spritedir;
			spritedir = cmd.getOptionValue("sprites");

			if (spritedir == null)
			{
				spritedir = "dumps/sprites";
			}

			System.out.println("[*] Dumping sprites to: " + spritedir);
			dumpSprites(store, new File(spritedir));
		} else if (cmd.hasOption("interfaces"))
		{
			String dir;
			dir = cmd.getOptionValue("interfaces");

			if (dir == null)
			{
				dir = "dumps/interfaces";
			}

			System.out.println("[*] Dumping interfaces to: " + dir);
			dumpInterfaces(store, new File(dir));
		} else if (cmd.hasOption("mapdata"))
		{
			String dir;
			dir = cmd.getOptionValue("mapdata");

			if (dir == null)
			{
				dir = "dumps/map-data";
			}

			System.out.println("[*] Dumping map data to: " + dir);
			dumpMapData(store, new File(dir));
		} else if (cmd.hasOption("mapimages"))
		{
			String dir;
			dir = cmd.getOptionValue("mapimages");

			if (dir == null)
			{
				dir = "dumps/map-images";
			}

			System.out.println("[*] Dumping map images to: " + dir);
			dumpMapImages(store, new File(dir));
		}
		else
		{
			System.err.println("[*] Nothing to do. Provide a command line argument to do something...");
			System.exit(-1);
			return;
		}
		
		System.out.println("[*] Finished.");
	}

	private static Store loadStore(String cache) throws IOException
	{
		Store store = new Store(new File(cache));
		store.load();
		return store;
	}

	private static Store loadStoreFlat(String cache) throws IOException
	{
		FlatStorage fs = new FlatStorage(new File(cache));
		Store store = new Store(fs);
		store.load();
		return store;
	}

	private static void dumpItems(Store store, File itemdir) throws IOException
	{
		ItemManager dumper = new ItemManager(store);
		dumper.load();
		dumper.export(itemdir);
		dumper.java(itemdir);
	}

	private static void dumpNpcs(Store store, File npcdir) throws IOException
	{
		NpcManager dumper = new NpcManager(store);
		dumper.load();
		dumper.dump(npcdir);
		dumper.java(npcdir);
	}

	private static void dumpObjects(Store store, File objectdir) throws IOException
	{
		ObjectManager dumper = new ObjectManager(store);
		dumper.load();
		dumper.dump(objectdir);
		dumper.java(objectdir);
	}

	private static void dumpModels(Store store, File modelDir) throws IOException
	{
		modelDir.mkdirs();
		Storage storage = store.getStorage();
		Index index = store.getIndex(IndexType.MODELS);

		for (Archive archive : index.getArchives())
		{
			int modelIndex = archive.getArchiveId();

			byte[] contents = archive.decompress(storage.loadArchive(archive));

			TextureManager tm = new TextureManager(store);
			tm.load();

			ModelLoader loader = new ModelLoader();

			ModelDefinition model;
			try
			{
				model = loader.load(archive.getArchiveId(), contents);
			}
			catch (NullPointerException ex)
			{
				System.err.println("[*] Error extracting models: " + ex.getMessage());
				System.out.println("[*] Try using the default flatcache provided with the project");
				System.exit(-1);
				return;
			}

			ObjExporter exporter = new ObjExporter(tm, model);

			String objFileOut = modelDir + File.separator + modelIndex + ".obj";
			String mtlFileOut = modelDir + File.separator + modelIndex + ".mtl";

			try (PrintWriter objWriter = new PrintWriter(new FileWriter(new File(objFileOut)));
			PrintWriter mtlWriter = new PrintWriter(new FileWriter(new File(mtlFileOut))))
			{
				exporter.export(objWriter, mtlWriter);
			}
		}
	}

	private static void dumpSprites(Store store, File dir) throws IOException
	{
		dir.mkdirs();
		SpriteManager dumper = new SpriteManager(store);
		dumper.load();
		dumper.export(dir);
	}

	private static void dumpInterfaces(Store store, File dir) throws IOException
	{
		InterfaceManager dumper = new InterfaceManager(store);
		dumper.load();
		dumper.export(dir);
		dumper.java(dir);
	}

	private static void dumpMapData(Store store, File dir) throws IOException {
		dir.mkdirs();

		GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
		Gson gson = builder.create();

		// Underlays

		UnderlayManager underlayDumper = new UnderlayManager(store);
		underlayDumper.load();
		Collection<UnderlayDefinition> underlays = underlayDumper.getUnderlays();
		for (UnderlayDefinition def : underlays) {
			def.calculateHsl(); // TODO: Collections.unmodifiableCollection wont change anything, already done in OverlayLoader ?
		}

		String underlayStr = gson.toJson(underlays);

		File underlayFile = new File(dir, "underlays.json");
		try (FileWriter fw = new FileWriter(underlayFile)) {
			fw.write(underlayStr);
		}

//		for (UnderlayDefinition def : underlays)
//		{
//			GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
//			Gson gson = builder.create();
//			String underlayStr = gson.toJson(def);
//
//			File targ = new File(dir, def.id + ".json");
//			try (FileWriter fw = new FileWriter(targ))
//			{
//				fw.write(underlayStr);
//			}
//		}

		// Overlays

		OverlayManager overlayDumper = new OverlayManager(store);
		overlayDumper.load();
		Collection<OverlayDefinition> overlays = overlayDumper.getOverlays();
		for (OverlayDefinition def : overlays) {
			def.calculateHsl(); // TODO: Collections.unmodifiableCollection wont change anything, already done in OverlayLoader ?
		}

		String overlayStr = gson.toJson(overlays);

		File overlayFile = new File(dir, "overlays.json");
		try (FileWriter fw = new FileWriter(overlayFile)) {
			fw.write(overlayStr);
		}

		// Area data

		AreaManager areaManager = new AreaManager(store);
		areaManager.load();

		for (AreaDefinition area : areaManager.getAreas()) {
			File areaFile = new File(dir, "area-" + area.id + ".json");
			try (FileWriter fw = new FileWriter(areaFile)) {
				fw.write(underlayStr);
			}
		}

		// World map data

		Index index = store.getIndex(IndexType.WORLDMAP);
		Archive archive = index.getArchive(0); // there is also archive 1/2, but their data format is not this

		Storage storage = store.getStorage();
		byte[] archiveData = storage.loadArchive(archive);
		ArchiveFiles files = archive.getFiles(archiveData);

		for (FSFile file : files.getFiles()) {
			WorldMapLoader loader = new WorldMapLoader();
			WorldMapDefinition def = loader.load(file.getContents(), file.getFileId());

			File areaFile = new File(dir, "worldmap-" + file.getFileId() + ".json");
			try (FileWriter fw = new FileWriter(areaFile)) {
				fw.write(gson.toJson(def));
			}
		}

	}

	private static void dumpMapImages(Store store, File dir) throws IOException
	{
		dir.mkdirs();

		// Map images

		MapImageDumper mapDumper = new MapImageDumper(store);
		mapDumper.load();
		for (int z = 0; z < Region.Z; ++z)
		{
			BufferedImage image = mapDumper.drawMap(z);
			File imageFile = new File(dir, "map-" + z + ".png");
			ImageIO.write(image, "png", imageFile);
		}

		// Region images

		RegionLoader regionLoader = new RegionLoader(store);
		regionLoader.loadRegions();
		for (int z = 0; z < Region.Z; ++z) {
			for (Region region : regionLoader.getRegions()) {
				File imageFile = new File(dir, "region-" + z + "-" + region.getRegionID() + ".png");
				BufferedImage image = mapDumper.drawRegion(region, z);
				ImageIO.write(image, "png", imageFile);
			}
		}

		// Heightmap images

		HeightMapDumper heightmapDumper = new HeightMapDumper(store);
		heightmapDumper.load();

		for (int z = 0; z < Region.Z; ++z) {
			BufferedImage image = heightmapDumper.drawHeightMap(z);
			File imageFile = new File(dir, "heightmap-" + z + ".png");
			ImageIO.write(image, "png", imageFile);
		}
	}
}
