/*
 * Copyright (C) 2006 Steve Ratcliffe
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 * 
 * Author: Steve Ratcliffe
 * Create date: 03-Dec-2006
 */
package uk.me.parabola.imgfmt.app;

import java.io.FileNotFoundException;

import uk.me.parabola.imgfmt.FileSystemParam;
import uk.me.parabola.imgfmt.sys.FileSystem;

import org.apache.log4j.Logger;

/**
 * Holder for a complete map.  A map is made up of several files.
 * <p>Needless to say, it has nothing to do with java.util.Map.
 *
 * @author Steve Ratcliffe
 */
public class Map {
	private static final Logger log = Logger.getLogger(Map.class);

    private static final int MAP_POINT = 0;
    private static final int MAP_INDEXED_POINT = 1;
    private static final int MAP_LINE = 2;
    private static final int MAP_SHAPE = 3;

	private FileSystem fileSystem;

	private TREFile treFile;
	private RGNFile rgnFile;
	private LBLFile lblFile;

    private int lastMapElement;

	// Use createMap() or loadMap() instead of creating a map directly.
	private Map() {
	}

	/**
	 * Create a complete map.  This consists of (at least) three
	 * files that all have the same basename and different extensions.
	 *
	 * @param mapname The name of the map.  This is an 8 digit number as a
	 * string.
	 * @param params Parameters that describe the file system that the map
	 * will be created in.
	 * @return A map object that holds together all the files that make it up.
	 */
	public static Map createMap(String mapname, FileSystemParam params) {
		Map m = new Map();
		try {
			m.fileSystem = new FileSystem(mapname + ".img", params);
			m.rgnFile = new RGNFile(m.fileSystem.create(mapname + ".RGN"));
			m.treFile = new TREFile(m.fileSystem.create(mapname + ".TRE"));
			m.lblFile = new LBLFile(m.fileSystem.create(mapname + ".LBL"));

			m.treFile.setMapId(Integer.parseInt(mapname));

		} catch (FileNotFoundException e) {
			log.error("failed to create file", e);
			return null;
		}

		return m;
	}

    /**
     * Set the area that the map covers.
     * @param area The outer bounds of the map.
     */
    public void setBounds(Area area) {
        treFile.setBounds(area); 
    }

    /**
     * Add a copyright message to the map.
     * @param str the copyright message. The second (last?) one set
     * gets shown when the device starts (sometimes?).
     */
    public void addCopyright(String str) {
        Label cpy = lblFile.newLabel(str);
        treFile.addCopyright(cpy);
    }

    /**
     * There is an area after the TRE header and before its data
     * starts that can be used to save any old junk it seems.
     *
     * @param info Any string.
     */
    public void addInfo(String info) {
        treFile.addInfo(info);
    }

    /**
     * Create a new zoom level. The level 0 is the most detailed and
     * level 15 is the most general.  Most maps would just have 4
     * different levels or less.  We are just having two to start with
     * but will probably advance to at least 3.
     *
     * @param level The zoom level, and integer between 0 and 15. Its
     * like a logical zoom level.
     * @param bits  The number of bits per coordinate, a measure of
     * the actual amount of detail that will be in the level.  So this
     * is like a physical zoom level.
     */
    public Zoom createZoom(int level, int bits) {
        return treFile.createZoom(level, bits);
    }

    /**
     * Create the top level division. It must be empty afaik and cover
     * the whole area of the map.
     *
     * @param area The whole map area.
     * @param zoom The zoom level that you want the top level to be
     * at.  Its going to be at least level 1.
     */
    public Subdivision topLevelSubdivision(Area area, Zoom zoom) {
		zoom.setInherited(true); // May not always be necessary/desired

        Subdivision sub = Subdivision.topLevelSubdivision(area, zoom);
		rgnFile.startDivision(sub);
        return sub;
    }

    /**
     * Create a subdivision that is beneath the top level.  We have to
     * pass the parent division.
     *
     * Note that you cannot create these all up front.  You must
     * create it, fill it will its map elements and then create the
     * next one.  You must also start at the top level and work down.
     *
     * @param sub The parent subdivision.
     * @param area The area of the new child subdiv.
     * @param zoom The zoom level of the child.
     */
    public Subdivision createSubdivision(Subdivision sub, Area area, Zoom zoom)
    {
        Subdivision child = sub.createSubdivision(area, zoom);
		log.debug("creating main division");
        return child;
    }

    public void addPointOverview(Overview ov) {
        treFile.addPointOverview(ov);
    }

    public void addPolylineOverview(Overview ov) {
        treFile.addPolylineOverview(ov);
    }

    public void addPolygonOverview(Overview ov) {
        treFile.addPolygonOverview(ov);
    }

    /**
     * Set the point of interest flags.
     * @param flags The POI flags.
     */
    public void setPoiDisplayFlags(int flags) {
        treFile.setPoiDisplayFlags((byte) flags);
    }

    public void startDivision(Subdivision sub) {
        rgnFile.startDivision(sub);
    }

    /**
     * We are starting to draw the points.  These must be done first.
     */
    public void startPoints() {
        if (lastMapElement > MAP_POINT)
            throw new IllegalStateException("Points must be drawn first");

        lastMapElement = MAP_POINT;

        rgnFile.setPointPtr();
    }

    /**
     * We are starting to draw the lines.  These must be done before
     * polygons.
     */
    public void startIndPoints() {
        if (lastMapElement > MAP_INDEXED_POINT)
            throw new IllegalStateException("Indexed points must be done before lines and polygons");

        lastMapElement = MAP_INDEXED_POINT;

        rgnFile.setPolylinePtr();
    }

    /**
     * We are starting to draw the lines.  These must be done before
     * polygons.
     */
    public void startLines() {
        if (lastMapElement > MAP_LINE)
            throw new IllegalStateException("Lines must be done before polygons");

        lastMapElement = MAP_LINE;

        rgnFile.setPolylinePtr();
    }

    /**
     * We are starting to draw the lines.  These must be done before
     * polygons.
     */
    public void startShapes() {

        lastMapElement = MAP_SHAPE;

        rgnFile.setPolygonPtr();
    }

    public Polyline createLine(Subdivision div, String name) {
        Label label = lblFile.newLabel(name);
        Polyline pl = new Polyline(div);

        pl.setLabel(label);
        return pl;
    }

    public Polygon createPolygon(Subdivision div, String name) {
        Label label = lblFile.newLabel(name);
        Polygon pg = new Polygon(div);

        pg.setLabel(label);
        return pg;
    }

    public Point createPoint(Subdivision div, String name) {
        Point p = new Point(div);
        Label label = lblFile.newLabel(name);

        p.setLabel(label);
        return p;
    }

    public void addMapObject(MapObject item) {
        rgnFile.addMapObject(item);
    }

    /**
	 * Close this map by closing all the constituent files.
	 */
	public void close() {
		treFile.setLastRgnPos(rgnFile.position() - 29);
		
		rgnFile.close();
		treFile.close();
		lblFile.close();

		fileSystem.close();
	}

}