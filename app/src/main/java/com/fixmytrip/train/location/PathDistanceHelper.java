package com.fixmytrip.train.location;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.Math;
/**
 * Created by philipkonieczny on 4/19/15.
 */
public class PathDistanceHelper {

    public double calculateDistance(PolylineOptions poly, Location location)
    {
        //TODO:May want to improve performance
        double distance = 99999999;
        bdccGeo p = new bdccGeo(location.getLatitude(),location.getLongitude());
        for(int i=0; i<(poly.getPoints().size()-1); i++)
        {
            LatLng point = poly.getPoints().get(i);
            LatLng nextPoint = poly.getPoints().get(i+1);

            bdccGeo l1 = new bdccGeo(point.latitude,point.longitude);
            bdccGeo l2 = new bdccGeo(nextPoint.latitude,nextPoint.longitude);
            double dp = p.distanceToLineSegMtrs(l1,l2);
            if(dp < distance)
                distance = dp;
        }
        return distance;
    }

    private class bdccGeo
    {
        private double x;
        private double y;
        private double z;

        // Constructor
        public bdccGeo(double lat, double lon)
        {
            double theta = (lon * Math.PI / 180.0);
            double rlat = bdccGeoGeocentricLatitude(lat * Math.PI / 180.0);
            double c = Math.cos(rlat);
            this.x = c * Math.cos(theta);
            this.y = c * Math.sin(theta);
            this.z = Math.sin(rlat);
        }

        // Convert latitude
        public double bdccGeoGeocentricLatitude(double geographicLatitude)
        {
            double flattening = 1.0 / 298.257223563; //WGS84
            double f = (1.0 - flattening) * (1.0 - flattening);
            return Math.atan((Math.tan(geographicLatitude) * f));
        }

        // Returns the two antipodal points of intersection of two great
        // circles defined by the arcs geo1 to geo2 and
        // geo3 to geo4. Returns a point as a Geo, use .antipode to get the other point
        public bdccGeo bdccGeoGetIntersection(bdccGeo geo1, bdccGeo geo2, bdccGeo geo3, bdccGeo geo4)
        {
            bdccGeo geoCross1 = geo1.crossNormalize(geo2);
            bdccGeo geoCross2 = geo3.crossNormalize(geo4);
            return geoCross1.crossNormalize(geoCross2);
        }

        // radians to Meters
        public double bdccGeoRadiansToMeters(double rad)
        {
            return rad * 6378137.0; // WGS84 - Radio Ecuatorial en metros
        }

        public double dot(bdccGeo b)
        {
            return ((this.x * b.x) + (this.y * b.y) + (this.z * b.z));
        }

        public double crossLength (bdccGeo b)
        {
            double x = (this.y * b.z) - (this.z * b.y);
            double y = (this.z * b.x) - (this.x * b.z);
            double z = (this.x * b.y) - (this.y * b.x);
            return Math.sqrt((x * x) + (y * y) + (z * z));
        }

        public bdccGeo scale(double s)
        {
            bdccGeo r = new bdccGeo(0,0);
            r.x = this.x * s;
            r.y = this.y * s;
            r.z = this.z * s;
            return r;
        }

        public bdccGeo crossNormalize(bdccGeo b)
        {
            double x = (this.y * b.z) - (this.z * b.y);
            double y = (this.z * b.x) - (this.x * b.z);
            double z = (this.x * b.y) - (this.y * b.x);
            double L = Math.sqrt((x * x) + (y * y) + (z * z));
            bdccGeo r = new bdccGeo(0,0);
            r.x = x / L;
            r.y = y / L;
            r.z = z / L;
            return r;
        }

        public bdccGeo antipode()
        {
            return this.scale(-1.0);
        }

        // Distance in radians to point 2
        public double distance(bdccGeo p2)
        {
            return Math.atan2(p2.crossLength(this), p2.dot(this));
        }

        // Returns in meters the minimum of the perpendicular distance of this point from the line segment geo1-geo2
        // and the distance from this point to the line segment ends in geo1 and geo2
        private double distanceToLineSegMtrs(bdccGeo geo1, bdccGeo geo2)
        {

            // Point on unit sphere above origin and normal to plane of geo1, geo2
            // could be either side of the plane
            bdccGeo p2 = geo1.crossNormalize(geo2);

            // intersection of GC normal to geo1/geo2 passing through p with GC geo1/geo2
            bdccGeo ip = bdccGeoGetIntersection(geo1,geo2,this,p2);

            //need to check that ip or its antipode is between p1 and p2
            double d = geo1.distance(geo2);
            double d1p = geo1.distance(ip);
            double d2p = geo2.distance(ip);

            if ((d >= d1p) && (d >= d2p))
                return bdccGeoRadiansToMeters(this.distance(ip));
            else
            {
                ip = ip.antipode();
                d1p = geo1.distance(ip);
                d2p = geo2.distance(ip);
            }
            if ((d >= d1p) && (d >= d2p))
                return bdccGeoRadiansToMeters(this.distance(ip));
            else
                return bdccGeoRadiansToMeters(Math.min(geo1.distance(this),geo2.distance(this)));
        }

    }

}