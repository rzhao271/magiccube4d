// 2 # 1 "com/donhatchsw/util/Poly.prejava"
// 3 # 1 "<built-in>"
// 4 # 1 "<command line>"
// 5 # 1 "com/donhatchsw/util/Poly.prejava"
//
// Poly.prejava
//
// Author: Don Hatch (hatch@hadron.org)
// This code may be used for any purpose as long as it is good and not evil.
//

package com.donhatchsw.util;

// 15 # 1 "com/donhatchsw/util/macros.h" 1
//
// macros.h
//
// 19 # 32 "com/donhatchsw/util/macros.h"
// XXX change the following to PRINTARRAY I think
// 21 # 11 "com/donhatchsw/util/Poly.prejava" 2

/**
 * A Poly consists of an array of vertices
 * and a ragged multidimensional array of indices into the vertices;
 * it usually represents a polygon, polyhedron,
 * or (heirarchical) list of polyhedra.
 * <p>
 * The type of each vertex is Object (typically double[], but can be anything).
 */
public class Poly
{
    /**
     * Array of Vertices.
     * Usually double[] but can be any Object.
     */
    public Object verts[/*nVerts*/];

    /**
     * Indices into verts.
     * The inds array can be any of the following, with typical interpretations:
     * <ul>
     *     <li> Integer is a single vertex. </li>
     *     <li> int[] is a single contour. </li>
     *     <li> int[][] is a single (multi-contour) polygon. </li>
     *     <li> int[][][] is a single polyhedron. </li>
     *     <li> int[][][][] is a list of polyhedra. </li>
     *     <li> int[][][][][] is a list of lists of polyhedra, etc. </li>
     * </ul>
     */
    public Object inds;
    /**
     * The aux array is an array of dimension &le; the dimension of inds,
     * associated with inds.
     * It is typically null, but if set,
     * operations will repeat/concatenate these arrays
     * in the same way as they repeat/concatenate inds.
     * Typical use is to store colors
     * (which can be associated with any level of the heirarchy)
     * or back indices into some original array
     * (also associated with any level of the heirarchy).
     */
    public Object aux;
    /**
     * XXX just a place to store them; they are currently not retained
     *     in any operations
     */
    public double repeatVectors[][/*vertexDim*/];

    /** Note, shallow copy! */
    public Poly(
                Object verts[],
                Object inds,
                Object aux,
                double repeatVectors[][])
    {
        this.verts = verts;
        this.inds = inds;
        this.aux = aux;
        this.repeatVectors = repeatVectors;
    } // Poly ctor

    /** Flattens (or expands) inds into a ragged array of ints of the desired dimension; the result must be cast to the desired type. */
    public Object getInds(int desiredDim)
    {
        return expandOrFlatten(inds, desiredDim);
    }

    /** Flattens (or expands) inds into a 1-dimensional array of ints. */
    public int[] getInds1() {return (int[])getInds(1);}
    /** Flattens (or expands) inds into a 2-dimensional ragged array of ints. */
    public int[][] getInds2() {return (int[][])getInds(2);}
    /** Flattens (or expands) inds into a 3-dimensional ragged array of ints. */
    public int[][][] getInds3() {return (int[][][])getInds(3);}
    /** Flattens (or expands) inds into a 4-dimensional ragged array of ints. */
    public int[][][][] getInds4() {return (int[][][][])getInds(4);}
    /** Flattens (or expands) inds into a 5-dimensional ragged array of ints. */
    public int[][][][][] getInds5() {return (int[][][][][])getInds(5);}
    // etc., if anyone cares

    /**
     * XXX desiredDim is a kind of bad way of specifying the dimension,
     *     since it violates opacity of aux, sort of. :-(
     */
    public Object getAux(int desiredDim)
    {
        return expandOrFlatten(aux, desiredDim);
    }

    /**
     * Return a new Poly
     * whose index list has p's index list as its single element.
     */
    public static Poly singleton(Poly p)
    {
        if (p == null)
        {
            return new Poly(new Object[0],
                            new int[0], // XXX?! fuck fuck fuck, I expect assertion failures when combining.  this idea just isn't going to cut it
                            null,
                            null);
        }
        else
        {
            Object pInds = p.inds;
            Object newInds = Arrays.singleton(pInds);

            Object pAux = p.aux;
            Object newAux;
            if (pAux == null)
                newAux = null;
            else
                newAux = Arrays.singleton(pAux);

            return new Poly(p.verts,
                            newInds,
                            newAux,
                            p.repeatVectors);
        }
    } // singleton


    /**
     * XXX hmm... concatenation of singletons seems to be the most common
     *     way this is used... should I make a function for that?
     */
    public static Poly concat(Poly polys[])
    {
        int nPolys = polys.length;
        do { if (!(nPolys >= 1)) throw new Error("Assumption failed at "+"com/donhatchsw/util/Poly.prejava"+"("+139 +"): " + "nPolys >= 1" + ""); } while (false); // otherwise how can we know dimensions?
        Class vertType = null, indsType = null, auxType = null;
        {
            for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
            {
                Poly poly = polys[iPoly];
                if (poly != null)
                {
                    vertType = poly.verts.getClass().getComponentType();
                    indsType = poly.inds.getClass();
                    auxType = (poly.aux == null ? null : polys[0].aux.getClass());
                    break;
                }
            }
        }
        if (indsType == null)
            return null; // all null XXX this is kind of ad-hoc
        do { if (!(vertType != null)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+156 +"): " + "vertType != null" + ""); } while (false);
        int nAux = 0;
        int nVerts, nInds;
        {
            nVerts = 0; // and counting
            nInds = 0; // and counting
            for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
            {
                Poly poly = polys[iPoly];
                if (poly != null)
                {
                    nVerts += poly.verts.length;
                    do { if (!(poly.inds.getClass() == indsType)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+168 +"): " + "poly.inds.getClass() == indsType" + ""); } while (false);
                    nInds += java.lang.reflect.Array.getLength(poly.inds);
                    do { if (!((poly.aux != null) == (auxType != null))) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+170 +"): " + "(poly.aux != null) == (auxType != null)" + ""); } while (false);
                    if (auxType != null)
                    {
                        do { if (!(poly.aux.getClass() == auxType)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+173 +"): " + "poly.aux.getClass() == auxType" + ""); } while (false);
                        nAux += java.lang.reflect.Array.getLength(poly.aux);
                    }
                }
            }
        }
        Object verts[] = (Object[])java.lang.reflect.Array.newInstance(vertType, nVerts);
        Object inds = java.lang.reflect.Array.newInstance(indsType.getComponentType(), nInds);
        Object aux = (auxType == null ? null : java.lang.reflect.Array.newInstance(auxType.getComponentType(), nAux));
        {
            int vertsOffset = 0;
            int indsOffset = 0;
            int auxOffset = 0;
            for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
            {
                Poly poly = polys[iPoly];
                if (poly != null)
                {
                    // do inds before verts, so vertsOffset will be right...
                    {
                        Object indsThisPoly = poly.inds;
                        int nIndsThisPoly = java.lang.reflect.Array.getLength(poly.inds);
                        copyInds(inds, indsOffset,
                                 indsThisPoly, 0,
                                 nIndsThisPoly,
                                 vertsOffset);
                        indsOffset += nIndsThisPoly;
                    }
                    {
                        Object vertsThisPoly[] = poly.verts;
                        int nVertsThisPoly = vertsThisPoly.length;
                        System.arraycopy(poly.verts, 0,
                                         verts, vertsOffset,
                                         nVertsThisPoly);
                        vertsOffset += nVertsThisPoly;
                    }
                    if (auxType != null)
                    {
                        Object auxThisPoly = poly.aux;
                        int nAuxThisPoly = java.lang.reflect.Array.getLength(auxThisPoly); // XXX should be nIndsThisPoly, I think maybe
                        System.arraycopy(auxThisPoly, 0,
                                         aux, auxOffset,
                                         nAuxThisPoly);
                        auxOffset += nAuxThisPoly;
                    }
                }
            }
            do { if (!(vertsOffset == nVerts)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+220 +"): " + "vertsOffset == nVerts" + ""); } while (false);
            do { if (!(indsOffset == nInds)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+221 +"): " + "indsOffset == nInds" + ""); } while (false);
            do { if (!(auxOffset == nAux)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+222 +"): " + "auxOffset == nAux" + ""); } while (false);
        }

        return new Poly(verts, inds, aux,
                        null); // repeat vectors lost
    } // concat

    public static Poly concat(Poly p0,
                              Poly p1)
    {
        return concat(new Poly[] {p0,p1});
    } // concat


    /**
     * Make a concatenation of n copies of p0*initM,
     * transformed by incM, incM^2, ...incM^(n-1)
     * where incM is a row-oriented transformation matrix
     * (i.e  apply it on the right to a row vector on the left
     * to get a new row vector).
     * The vertex type must be double[].
     */
    public static Poly repeat(int n,
                              Poly p0,
                              double initM[][], double incM[][])
    {
        double verts0[][] = (double[][])p0.verts;
        int nVerts0 = verts0.length;
        Object inds0 = p0.inds;
        int nInds0 = java.lang.reflect.Array.getLength(inds0);
        Class indsComponentType = inds0.getClass().getComponentType();
        do { if (!(indsComponentType != null)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+253 +"): " + "indsComponentType != null" + ""); } while (false); // must be a list
        Object aux0 = p0.aux;
        int nAux0 = (aux0 == null ? 0 : java.lang.reflect.Array.getLength(aux0));

        int nVerts = n * nVerts0;
        double verts[][] = new double[nVerts][];
        int nInds = n * nInds0;
        Object inds = java.lang.reflect.Array.newInstance(indsComponentType,
                                                          nInds);
        int nAux = n * nAux0;
        Object aux = (aux0 == null ? null
                                   : java.lang.reflect.Array.newInstance(aux0.getClass().getComponentType(),
                                                                         nAux));

        int vertsOffset = 0, facesOffset = 0, auxOffset = 0;

        double M[][] = initM;
        int i;
        for (i = 0; (i) < (n); ++i)
        {
            transformVerts(verts,
                           vertsOffset,
                           verts0,
                           M);
            copyInds(inds, facesOffset,
                     inds0, 0,
                     nInds0,
                     vertsOffset);
            if (aux0 != null)
                System.arraycopy(aux0, 0,
                                 aux, auxOffset,
                                 nAux0);
            vertsOffset += nVerts0;
            facesOffset += nInds0;
            auxOffset += nAux0;
            if (i+1 < n)
                M = VecMath.mxm(M, incM);
        }

        do { if (!(vertsOffset == nVerts)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+292 +"): " + "vertsOffset == nVerts" + ""); } while (false);
        do { if (!(facesOffset == nInds)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+293 +"): " + "facesOffset == nInds" + ""); } while (false);
        do { if (!(auxOffset == nAux)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+294 +"): " + "auxOffset == nAux" + ""); } while (false);

        return new Poly(verts, inds, aux, p0.repeatVectors);
    } // repeat

    /** vertex type must be double[] */
    public static Poly transform(Poly p0,
                                 double M[][])
    {
        double repeatVectors[][] = p0.repeatVectors;
        do { if (!(repeatVectors == null || repeatVectors.length == 0)) throw new Error("Assumption failed at "+"com/donhatchsw/util/Poly.prejava"+"("+305 +"): " + "repeatVectors == null || repeatVectors.length == 0" + ""); } while (false); // XXX repeat vectors should be transformed by the rotate/scale part of M, so do that if anyone ever needs it.

        double verts0[][] = (double[][])p0.verts;



        double verts[][];
        {
            int nVerts = verts0.length;
            verts = new double[nVerts][];
            for (int iVert = 0; (iVert) < (nVerts); ++iVert)
                verts[iVert] = VecMath.vxm(verts0[iVert], M);
        }

        return new Poly(verts,
                        p0.inds,
                        p0.aux,
                        repeatVectors);
    } // transform

    /** vertex type must be double[] */
    public static Poly simplify(Poly p,
                                double eps)
    {
        Triangulator triangulator = new Triangulator();

        double verts[][] = (double[][])p.verts;
        int indsDim = Arrays.getDim(p.inds);
        Object inds = Arrays.copy(p.inds, indsDim);
        {
            int cells[][][][] = (int[][][][])Arrays.flatten(inds, 0, (indsDim-4)+1); // XXX clearly shows the less-than-clear API
            int nCells = cells.length;
            for (int iCell = 0; (iCell) < (nCells); ++iCell)
            {
                int polys[][][] = cells[iCell];
                int nPolys = polys.length;
                for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
                {
                    int contours[][] = polys[iPoly];
                    int simpleContours[][] = null;

                    try
                    {
                        simpleContours = triangulator.simplify(verts,
                                                               contours,
                                                               contours.length,
                                                               eps);
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();

                        {
                            //
                            // Cull away unused vertices,
                            // and dump the problem polygon.
                            //
                            Poly foo = new Poly(verts, contours, null, null);
                            foo = separateAndCopyVerts(foo, 2, true);
                            System.out.println("foo.verts.length" + " = " + (foo.verts.length));
                            System.out.println("foo.verts" + " =\n" + Arrays.toStringNonCompact(foo.verts, "    ", "    "));
                            System.out.println("java.lang.reflect.Array.getLength(foo.inds)" + " = " + (java.lang.reflect.Array.getLength(foo.inds)));
                            System.out.println("foo.inds" + " = " + Arrays.toStringCompact(foo.inds));
                        }
                        throw new Error("triangulateor.simplify failed: "+e.toString());
                    }
                    polys[iPoly] = simpleContours;
                }
            }
        }
        return new Poly(verts,
                        inds,
                        p.aux,
                        p.repeatVectors);
    } // simplify

    /**
     * vertex type must be double[].
     * If doGroup is true, the number of dimensions gets increased by 1,
     * with each lowest-level triangle replaced by a list of triangles;
     * otherwise the new triangles lists are flattened so the number of
     * dimensions is unchanged.
     * If any finest-level arrays have length other than 3, they
     * are left alone (if doGroup = false) or made into singletons
     * (if doGroup = true).
     */
    public static Poly subdivideTriangles(Poly p,
                                          int n,
                                          boolean doGroup)
    {
        do { if (!(n >= 1)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+394 +"): " + "n >= 1" + ""); } while (false);
        int pIndsDim = Arrays.getDim(p.inds);
        Object inds = Arrays.flatten(Arrays.copy(p.inds,pIndsDim),
                                     pIndsDim-1,
                                     0); // i.e. expand each finest-level array into a singleton, i.e. put each triangle [3] into a singleton group [1][3]

        // triangular array of indices into verts, for temporary...
        int theseNewInds[][] = new int[n+1][];
        for (int i = 0; (i) < (n+1); ++i)
            theseNewInds[i] = new int[i+1];

        int groupGroups[][][][] = (int[][][][])Arrays.flatten(inds, 0, Arrays.getDim(inds)-4+1); // XXX clearly shows the less-than-clear API
        int nGroupGroups = groupGroups.length;

        int nTrisOriginally = Arrays.arrayLength(p.inds, pIndsDim-1); // assuming they are all tris

        double pVerts[][] = (double[][])p.verts;
        int maxVerts = nTrisOriginally * (((n+1)*((n+1)+1)) / 2)
                     + Arrays.arrayLength(p.inds, pIndsDim); // for non-triangles
        double verts[][] = new double[maxVerts][];
        int nVerts = 0; // and counting

        int oldToNewVerts[] = VecMath.fillvec(p.verts.length, -1);
        int edges[][][] = new int[p.verts.length][0][];
        int edgeEnd[][] = new int[p.verts.length][0];

        for (int iGroupGroup = 0; (iGroupGroup) < (nGroupGroups); ++iGroupGroup)
        {
            int groupGroup[][][] = groupGroups[iGroupGroup];
            int nGroups = groupGroup.length;
            for (int iGroup = 0; (iGroup) < (nGroups); ++iGroup)
            {
                int group[][] = groupGroup[iGroup];
                do { if (!(group.length == 1)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+427 +"): " + "group.length == 1" + ""); } while (false);
                int tri[] = group[0];
                for (int i = 0; (i) < (tri.length); ++i)
                    if (oldToNewVerts[tri[i]] == -1)
                    {
                        verts[nVerts] = pVerts[tri[i]];
                        oldToNewVerts[tri[i]] = nVerts;
                        //System.out.print("0");
                        nVerts++;
                    }
                if (tri.length == 3)
                {
                    for (int i = 0; (i) < (3); ++i)
                    {
                        int i0 = tri[i];
                        int i1 = tri[(i+1)%3];
                        int iMin = ((i0)<=(i1)?(i0):(i1));
                        int iMax = ((i0)>=(i1)?(i0):(i1));
                        int iNeighbor;
                        for (iNeighbor = 0; (iNeighbor) < (edgeEnd[iMin].length); ++iNeighbor)
                            if (edgeEnd[iMin][iNeighbor] == iMax)
                                break; // found this edge
                        if (iNeighbor == edgeEnd[iMin].length)
                        {
                            // didn't find it; append new subdivided edge
                            edgeEnd[iMin] = Arrays.append(edgeEnd[iMin], iMax);
                            edges[iMin] = (int[][])Arrays.append(edges[iMin], new int[n+1]);
                            edges[iMin][iNeighbor][0] = oldToNewVerts[iMin];
                            for (int k = 1; k <= n-1; ++k)
                            {
                                verts[nVerts] = VecMath.lerp(pVerts[iMin],
                                                             pVerts[iMax],
                                                             (double)k/n);
                                edges[iMin][iNeighbor][k] = nVerts;
                                //System.out.print("1");
                                nVerts++;
                            }
                            edges[iMin][iNeighbor][n] = oldToNewVerts[iMax];
                        }
                        int edge[] = edges[iMin][iNeighbor];
                        for (int k = 0; (k) < (n+1); ++k)
                        {
                            theseNewInds[i==0 ? k : i==1 ? n : n-k]
                                        [i==0 ? 0 : i==1 ? k : n-k]
                                            = edge[i0<i1 ? k : n-k];
                        }
                    }

                    double v0[] = pVerts[tri[0]];
                    double v1[] = pVerts[tri[1]];
                    double v2[] = pVerts[tri[2]];

                    // Create the internal vertices...
                    for (int i = 1; i <= n-1; ++i)
                    for (int j = 1; j <= i-1; ++j)
                    {
                        verts[nVerts] = VecMath.bary(v0,
                                                     v1, (double)(i-j)/n,
                                                     v2, (double)j/n);
                        theseNewInds[i][j] = nVerts;
                        //System.out.print("2");
                        nVerts++;
                    }

                    // replace the group consisting of a single tri
                    // with a group of n*n tris...
                    groupGroup[iGroup] = group = new int[n*n][3];
                    int indInGroup = 0;
                    for (int i = 0; (i) < (n); ++i)
                    for (int j = 0; (j) < (i+1); ++j)
                    {
                        group[indInGroup][0] = theseNewInds[i][j];
                        group[indInGroup][1] = theseNewInds[i+1][j];
                        group[indInGroup][2] = theseNewInds[i+1][j+1];
                        indInGroup++;
                        if (j < i)
                        {
                            group[indInGroup][0] = theseNewInds[i+1][j+1];
                            group[indInGroup][1] = theseNewInds[i][j+1];
                            group[indInGroup][2] = theseNewInds[i][j];
                            indInGroup++;
                        }
                    }
                    do { if (!(indInGroup == n*n)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+510 +"): " + "indInGroup == n*n" + ""); } while (false);
                }
                else
                {
                    // not a triangle... just copy the original over
                    for (int i = 0; (i) < (tri.length); ++i)
                        tri[i] = oldToNewVerts[tri[i]];
                }
            }
        }
        //System.out.println("!");

        verts = (double[][])Arrays.subarray(verts, 0, nVerts);

        if (!doGroup)
        {
            inds = Arrays.flatten(inds,
                                  Arrays.getDim(inds)-3,
                                  2);
        }

        //PRINT(p.verts.length);
        //PRINT(verts.length);

        return new Poly(verts,
                        inds,
                        p.aux,
                        p.repeatVectors);
    } // subdivideTriangles


    /**
     * Express the points p barycentrically in terms of the vertices v
     * of a convex polygon, from Joe Warren's paper on the web:
     * "Barycentric coordinates for convex sets",
     * modified for robustness.
     */
    public static void calcPolygonBary(
                                double v[][], // fixed convex boundary contour
                                double p[][], // points inside face
                                double result[/*nv*/][/*np*/])
    {
        int nv = v.length;
        int np = p.length;
        int dim = v[0].length;
        double E[][] = new double[nv][dim]; // E[i] = edge vec from v[i] to v[i+1]
        double N[][] = new double[nv][dim]; // N[i] = normal of E[i], not unit
        double lengthsqrdN[] = new double[nv];
        double c[] = new double[nv]; // N[i] dot v[i]
        for (int i = 0; (i) < (nv); ++i)
            VecMath.vmv(E[i], v[(i+1)%nv], v[i]);
        for (int i = 0; (i) < (nv); ++i)
        {
            VecMath.vmv(N[i], E[(i-1+nv)%nv],E[(i+1)%nv]);
            // subtract off the part parallel to E[i]...
            VecMath.vpsxv(N[i],
                          N[i], -VecMath.dot(N[i],E[i])
                                /VecMath.dot(E[i],E[i]), E[i]);
            lengthsqrdN[i] = VecMath.normsqrd(N[i]);
            c[i] = VecMath.dot(N[i], v[i]);
        }
        double numerator[] = new double[nv];
        for (int i = 0; (i) < (nv); ++i)
        {
            int iMinus1 = (i-1+nv)%nv;
            double temp = VecMath.dot(N[iMinus1], N[i]);
            numerator[i] = Math.sqrt(lengthsqrdN[iMinus1]*lengthsqrdN[i]
                                   - temp*temp);
        }

        double dist[] = new double[nv]; // scratch, dist from point to edge iv
        for (int ip = 0; (ip) < (np); ++ip)
        {
            double w[] = result[ip];

            for (int iv = 0; (iv) < (nv); ++iv)
                dist[iv] = c[iv] - VecMath.dot(N[iv], p[ip]);
            double sum = 0.;
            for (int iv = 0; (iv) < (nv); ++iv)
            {
                if (false) // prone to zero division!
                    w[iv] = numerator[iv] / (dist[(iv-1+nv)%nv] * dist[iv]);
                else // robust way (but more expensive)
                {
                    double prod = numerator[iv];
                    for (int jv = 0; (jv) < (nv-2); ++jv)
                        prod *= dist[(iv+1+jv)%nv]; // all except iv-1 and iv
                    w[iv] = prod;
                }
                sum += w[iv];
            }
            double invSum = 1./sum;
            for (int iv = 0; (iv) < (nv); ++iv)
                w[iv] *= invSum;

            // Okay, now we have the barycentric weights for p[ip]
            // in terms of v[0] .. v[nv-1].
            // If we did it correctly p[ip] should equal
            // the weighted sum [iv=0..nv-1] of the vertices w[iv]*v[iv].
            if (false) // debugging
            {
                double shouldBeP[] = VecMath.vxm(w, v);
                System.out.println("w.length" + " = " + (w.length));
                System.out.println("v.length" + " = " + (v.length));
                System.out.println("p[ip]" + " = " + VecMath.toString(p[ip]));
                System.out.println("shouldBeP" + " = " + VecMath.toString(shouldBeP));
            }
        }
    } // calcPolygonBary, caller allocated

    /**
     * Express the points p barycentrically in terms of the vertices v
     * of a convex polygon, from Joe Warren's paper on the web:
     * "Barycentric coordinates for convex sets",
     * modified for robustness.
     */
    public static double[][] calcPolygonBary(
                                double v[][], // fixed convex boundary contour
                                double p[][]) // points inside face
    {
        double result[][] = new double[p.length][v.length];
        calcPolygonBary(v, p, result);
        return result;
    } // calcPolygonBary, callee allocated

    /**
     * Returns a copy of tesselatedP
     * with vertices of untesselatedP fixed
     * and remaining vertices bowed towards the cell centers.
     * Vertex type must be double[].
     */
    public static Poly bowFaces(Poly untesselatedP,
                                Poly tesselatedP,
                                double exponent, // exponent to raise barycentric coords to... infinity means total collapse to cell center, 0 means shoot out to sum of verts from cell center XXX think about whether there is a more intuitive scale
                                int nIterations)
    {
        System.out.println("in bowFaces");
        System.out.println("    exponent="+exponent);
        System.out.println("    nIterations="+nIterations);

        //
        // Starting with the vertices of untesselatedP
        // together with its cell centers,
        // we want to move them so that they
        // attempt to solve a big system of simultaneous
        // equations:
        //      each cell center is the average of the cell's
        //          outer face vertices
        //      each vertex on the inner contour of a face
        //          is equal to a weighted average of the face's outer
        //          contour vertices and the cell center
        // Note that an inner contour vertex of one face
        // is an outer contour vertex of other faces.
        // Abstractly, each vertex (and cell center) wants to be
        // a weighted sum of other vertices (and cell centers).
        //

        int cells[][][][] = untesselatedP.getInds4();
        int nCells = cells.length;
        double verts[][] = (double[][])untesselatedP.verts;
        int nVerts = verts.length;
        int dim = verts[0].length;

        int nWorkVerts = nVerts // original verts
                       + nCells; // and cell centers

        int derivationInds[][] = new int[nWorkVerts][]; // which other verts a vert depends on
        double derivationWeights[][] = new double[nWorkVerts][]; // and their weights

        double workVerts[][] = new double[nWorkVerts][dim];
        {
            //
            // Fill the initial part of workVerts with the original vertices...
            //
            VecMath.copymat(workVerts, verts);

            //
            // Fill the rest of workVerts with the initial cell centers,
            // and record their derivation weights while we're at it...
            //
            {
                int tempVertexWeights[] = VecMath.fillvec(nVerts, 0); // yes, int
                for (int iCell = 0; (iCell) < (nCells); ++iCell)
                {
                    int polys[][][] = cells[iCell];
                    int nPolys = polys.length;

                    int tempVertexWeightsSum = 0; // and counting
                    int nDerivationParents = 0; // and counting
                    for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
                    {
                        int contours[][] = polys[iPoly];
                        int outerContour[] = contours[0];
                        int nVertsThisContour = outerContour.length;
                        for (int iVertThisContour = 0; (iVertThisContour) < (nVertsThisContour); ++iVertThisContour)
                        {
                            int iVert = outerContour[iVertThisContour];
                            if (tempVertexWeights[iVert] == 0)
                                nDerivationParents++;
                            tempVertexWeights[iVert]++;
                            tempVertexWeightsSum++;
                        }
                    }
                    derivationInds[nVerts+iCell] = new int[nDerivationParents];
                    derivationWeights[nVerts+iCell] = new double[nDerivationParents];
                    double invTempVertexWeightsSum = 1./tempVertexWeightsSum;
                    int iDerivationParent = 0;
                    for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
                    {
                        int contours[][] = polys[iPoly];
                        int outerContour[] = contours[0];
                        int nVertsThisContour = outerContour.length;
                        for (int iVertThisContour = 0; (iVertThisContour) < (nVertsThisContour); ++iVertThisContour)
                        {
                            int iVert = outerContour[iVertThisContour];
                            if (tempVertexWeights[iVert] != 0)
                            {
                                derivationInds[nVerts+iCell][iDerivationParent] = iVert;
                                derivationWeights[nVerts+iCell][iDerivationParent] = tempVertexWeights[iVert] * invTempVertexWeightsSum;
                                iDerivationParent++;
                                tempVertexWeights[iVert] = 0;
                            }
                        }
                    }
                    do { if (!(iDerivationParent == nDerivationParents)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+734 +"): " + "iDerivationParent == nDerivationParents" + ""); } while (false);
                    // now tempVertexWeights is all zeros again

                    double cellCenter[] = workVerts[nVerts+iCell];
                    VecMath.zerovec(cellCenter);
                    for (int i = 0; (i) < (nDerivationParents); ++i)
                        VecMath.vpsxv(cellCenter,
                                      cellCenter,
                                      derivationWeights[nVerts+iCell][i],
                                      verts[derivationInds[nVerts+iCell][i]]);
                }
            }

            //
            // Figure out derivation weights for each inner contour vert--
            // these are the barycentric coordinates of the inner contour vert
            // in terms of its face's outer contour verts.
            //
            {
                for (int iCell = 0; (iCell) < (nCells); ++iCell)
                {
                    int polys[][][] = cells[iCell];
                    int nPolys = polys.length;
                    for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
                    {
                        int contours[][] = polys[iPoly];
                        int nContours = contours.length;
                        int outerContour[] = contours[0];
                        int outerContourLength = outerContour.length;
                        double outerContourVerts[][] = (double[][])Arrays.getMany(
                                                               verts, outerContour);
                        // The derivation inds for every inner contour vertex
                        // on this polygon is the outer contour together with
                        // the cell center...
                        int outerContourAndCellCenter[] = Arrays.append(
                                                        outerContour, nVerts+iCell);

                        for (int iInnerContour = 0; (iInnerContour) < (nContours-1); ++iInnerContour)
                        {
                            int innerContour[] = contours[1+iInnerContour];
                            int innerContourLength = innerContour.length;
                            double innerContourVerts[][] = (double[][])Arrays.getMany(
                                                               verts, innerContour);
                            // Express innerContour in terms of outerContour...
                            // XXX should do all the inner contours at once, since calcPolygonBary's setup is expensive
                            double b[/*innerContourLength*/][/*outerContourLength*/]
                                            = calcPolygonBary(outerContourVerts,
                                                              innerContourVerts);
                            for (int iVertThisContour = 0; (iVertThisContour) < (innerContourLength); ++iVertThisContour)
                            {
                                int iVert = innerContour[iVertThisContour];
                                derivationInds[iVert] = outerContourAndCellCenter;
                                double sum = 0.;
                                for (int iVertOuterContour = 0; (iVertOuterContour) < (outerContourLength); ++iVertOuterContour)
                                {
                                    double bb = b[iVertThisContour][iVertOuterContour];
                                    bb = ((bb)>=(0.)?(bb):(0.)); // so pow won't bomb on slightly negative values with fractional exponent. XXX should assert it's only slightly negative
                                    bb = Math.pow(bb,exponent);
                                    b[iVertThisContour][iVertOuterContour] = bb;
                                    sum += bb;
                                }
                                double cellCenterWeight = 1.-sum;
                                derivationWeights[iVert] = Arrays.append(b[iVertThisContour], cellCenterWeight);
                            }
                        }
                    }
                }
            }
        }

        //
        // Iterate towards solution...
        //
        {
            double nextWorkVerts[][] = new double[nWorkVerts][dim];
            System.out.println("    "+nIterations+" iterations");
            System.out.print("        ");
            for (int iIter = 0; (iIter) < (nIterations); ++iIter)
            {
                System.out.print(""+iIter+" ");
                for (int iWorkVert = 0; (iWorkVert) < (nWorkVerts); ++iWorkVert)
                {
                    double nextWorkVert[] = nextWorkVerts[iWorkVert];
                    int inds[] = derivationInds[iWorkVert];
                    double weights[] = derivationWeights[iWorkVert];
                    if (inds == null)
                    {
                        // vertex doesn't depend on anything;
                        // it retains its old value
                        VecMath.copyvec(nextWorkVert,
                                        workVerts[iWorkVert]);
                    }
                    else
                    {
                        int nInds = inds.length;
                        VecMath.zerovec(nextWorkVert);
                        for (int iInd = 0; (iInd) < (nInds); ++iInd)
                            VecMath.vpsxv(nextWorkVert,
                                          nextWorkVert,
                                          weights[iInd],
                                          workVerts[inds[iInd]]);
                    }
                }
                double temp[][];
                {temp=(workVerts);workVerts=(nextWorkVerts);nextWorkVerts=(temp);};
            }
            System.out.println();
        }

        //
        // Okay, now workVerts contains the vertices of untesselatedP
        // and cell center, where we want them to be.
        // Now express each vertex of tesselatedP
        // barycentrically in terms of the original outer contour verts
        // in the corresponding face of the original untesselatedP,
        // and use those barycentric coords to interpolate workVerts.
        //
        double tesselatedVerts[][] = (double[][])tesselatedP.verts;
        int nTesselatedVerts = tesselatedVerts.length;
        double finalTesselatedVerts[][] = new double[nTesselatedVerts][]; // don't allocate the individual entries yet, so at each point we can determine if it's already been done
        {
            int tesselatedCells[][][][] = tesselatedP.getInds4();
            do { if (!(tesselatedCells.length == nCells)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+856 +"): " + "tesselatedCells.length == nCells" + ""); } while (false);
            // XXX very similar to what we did above-- maybe should combine code

            double interiornesses[] = new double[nTesselatedVerts];
            int cellThatWarpedMe[] = new int[nTesselatedVerts]; // not necessary, but for sanity checks
            int faceThatWarpedMe[] = new int[nTesselatedVerts]; // not necessary, but for sanity checks

            for (int iCell = 0; (iCell) < (nCells); ++iCell)
            {
                int polys[][][] = cells[iCell];
                int nPolys = polys.length;
                int tesselatedPolys[][][] = tesselatedCells[iCell];
                do { if (!(nPolys == tesselatedPolys.length)) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+868 +"): " + "nPolys == tesselatedPolys.length" + ""); } while (false);
                double cellCenter[] = workVerts[nVerts+iCell];

                for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
                {
                    int contours[][] = polys[iPoly];
                    int outerContour[] = contours[0];
                    int outerContourLength = outerContour.length;
                    double outerContourVerts[][] = (double [][])Arrays.getMany(
                                                      verts, outerContour);

                    int indsOfPointsInFinalTesselatedVerts[] = new int[0];

                    int tris[][] = tesselatedPolys[iPoly]; // not really necessarily tris, but they are either tris or contours
                    int nTris = tris.length;
                    for (int iTri = 0; (iTri) < (nTris); ++iTri)
                    {
                        int tri[] = tris[iTri];
                        int triLength = tri.length; // not necessarily a triangle
                        for (int iVertThisTri = 0; (iVertThisTri) < (triLength); ++iVertThisTri)
                        {
                            int iVert = tri[iVertThisTri];
                            // XXX this is inefficient-- should allocate once up front, and should notice sharing
                            indsOfPointsInFinalTesselatedVerts = Arrays.append(indsOfPointsInFinalTesselatedVerts, iVert);
                        }
                    }
                    double points[][] = (double[][])Arrays.getMany(tesselatedVerts, indsOfPointsInFinalTesselatedVerts);
                    double barys[/*nPoints*/][/*outerContourLength*/]
                            = calcPolygonBary(outerContourVerts,
                                              points);

                    // Bow the barycentric coords inwards
                    // towards cell center, and replace the final tesselated
                    // vert with the result (if more "interior"
                    // than any previous result for that vert).
                    int nPoints = points.length;
                    for (int iPoint = 0; (iPoint) < (nPoints); ++iPoint)
                    {
                        int iVert = indsOfPointsInFinalTesselatedVerts[iPoint];
                        double bary[] = barys[iPoint];
                        double interiorness = VecMath.productNotUsingLog(bary);
                        interiorness = Math.pow(((0)>=(interiorness)?(0):(interiorness)), 1./bary.length); // geom average XXX should we just use min and avoid the headache?!

                        // Sanity check...
                        {
                            if (finalTesselatedVerts[iVert] != null)
                            {
                                if (iCell != cellThatWarpedMe[iVert])
                                {
                                    //
                                    // On two different cells...
                                    // interiorness should be strictly
                                    // positive on one and zero on the other.
                                    //
                                    if (false)
                                        System.out.println(" +0 final vert "+iVert+" seen on cell "+cellThatWarpedMe[iVert]+" and "+iCell+", interiornesses "+interiornesses[iVert]+" and "+interiorness+"");
                                }
                                else if (iPoly != faceThatWarpedMe[iVert])
                                {
                                    //
                                    // On two different faces
                                    // of the same cell...
                                    // Both interiornesses should be zero.
                                    //
                                    if (false)
                                        System.out.println(" 00 final vert "+iVert+" seen on cell "+iCell+" on faces "+faceThatWarpedMe[iVert]+" and "+iPoly+" with interiornesses "+interiornesses[iVert]+" and "+interiorness+"");
                                }
                                else
                                {
                                    //
                                    // Seen again on same face...
                                    // interiorness should be exactly
                                    // the same as before.
                                    //
                                    if (false)
                                        System.out.println(" == final vert "+iVert+" seen twice on cell "+iCell+" face "+iPoly+" with interiornesses "+interiornesses[iVert]+" and "+interiorness+"");
                                }
                            }
                        }

                        if (finalTesselatedVerts[iVert] == null
                         || interiorness > interiornesses[iVert])
                        {
                            if (false)
                                System.out.println("     final vert "+iVert+": bary "+Arrays.toStringCompact(bary)+" -> "+interiorness+"");
                            double vert[] = VecMath.zerovec(dim);
                            finalTesselatedVerts[iVert] = vert;
                            interiornesses[iVert] = interiorness;
                            cellThatWarpedMe[iVert] = iCell;
                            faceThatWarpedMe[iVert] = iPoly;

                            double sum = 0.;
                            for (int iVertOuterContour = 0; (iVertOuterContour) < (outerContourLength); ++iVertOuterContour)
                            {
                                double b = bary[iVertOuterContour];
                                b = ((b)>=(0.)?(b):(0.)); // so pow won't bomb on slightly negative values with fractional exponent. XXX should assert it's only slightly negative
                                b = Math.pow(b, exponent);
                                VecMath.vpsxv(vert,
                                        vert,
                                        b,
                                        workVerts[outerContour[iVertOuterContour]]);
                                sum += b;
                            }
                            double cellCenterWeight = 1.-sum;
                            VecMath.vpsxv(vert,
                                          vert, cellCenterWeight, cellCenter);
                        }
                    }
                }
            }
        }

        System.out.println("out bowFaces");

        return new Poly(finalTesselatedVerts,
                        tesselatedP.inds,
                        tesselatedP.aux,
                        tesselatedP.repeatVectors);
    } // bowFaces


    /** vertex type must be double[] */
    public static Poly triangulate(Poly p,
                                   double eps,
                                   boolean optimize)
    {
        Triangulator triangulator = new Triangulator();

        double verts[][] = (double[][])p.verts;
        int indsDim = Arrays.getDim(p.inds);
        Object inds = Arrays.copy(p.inds, indsDim);
        {
            int cells[][][][] = (int[][][][])Arrays.flatten(inds, 0, (indsDim-4)+1); // XXX clearly shows the less-than-clear API
            int nCells = cells.length;
            for (int iCell = 0; (iCell) < (nCells); ++iCell)
            {
                int polys[][][] = cells[iCell];
                int nPolys = polys.length;
                for (int iPoly = 0; (iPoly) < (nPolys); ++iPoly)
                {
                    int contours[][] = polys[iPoly];
                    int tris[][] = null;

                    try
                    {
                        tris = triangulator.triangulate(verts,
                                                        contours,
                                                        contours.length,
                                                        eps,
                                                        optimize);
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();

                        {
                            //
                            // Cull away unused vertices,
                            // and dump the problem polygon.
                            //
                            Poly foo = new Poly(verts, contours, null, null);
                            foo = separateAndCopyVerts(foo, 2, true);
                            System.out.println("foo.verts.length" + " = " + (foo.verts.length));
                            System.out.println("foo.verts" + " =\n" + Arrays.toStringNonCompact(foo.verts, "    ", "    "));
                            System.out.println("java.lang.reflect.Array.getLength(foo.inds)" + " = " + (java.lang.reflect.Array.getLength(foo.inds)));
                            System.out.println("foo.inds" + " = " + Arrays.toStringCompact(foo.inds));
                        }
                        throw new Error("triangulate failed: "+e.toString());
                    }
                    polys[iPoly] = tris;
                }
            }
        }
        return new Poly(verts,
                        inds,
                        p.aux,
                        p.repeatVectors);
    } // triangulate



    /**
     * Find, for each vertex, a list of
     * other vertices adjacent to it.
     * The poly must be closed and oriented; that is,
     * each edge must appear exactly twice in opposite directions.
     * If the contours are CCW, the neighbors of each vertex will appear CW.
     * XXX not sure why I did it that way.
     * <p>
     * Currently, there is an additional restriction
     * that the vertex figure must be simple (i.e. its neighbors
     * form a single cycle around it).
     */
    public static int[][] computeAdjacencies(Poly p)
    {
        System.out.println("    in computeAdjacencies");

        int nVerts = p.verts.length;
        int neighborPairss[][][] = new int[nVerts][0][2];
        {
            int contours[][] = p.getInds2();
            int nContours = contours.length;
            for (int iContour = 0; (iContour) < (nContours); ++iContour)
            {
                int contour[] = contours[iContour];
                int nVertsThisContour = contour.length;
                for (int iVertThisContour = 0; (iVertThisContour) < (nVertsThisContour); ++iVertThisContour)
                {
                    int iVertPrev = contour[(iVertThisContour-1+nVertsThisContour)%nVertsThisContour];
                    int iVert = contour[iVertThisContour];
                    int iVertNext = contour[(iVertThisContour+1)%nVertsThisContour];
                    neighborPairss[iVert] = (int[][])Arrays.append(neighborPairss[iVert],
                                               new int[] {iVertPrev,iVertNext});
                }
            }
        }
        int adjs[][] = new int[nVerts][];
        {
            int nextNeighbor[] = VecMath.fillvec(nVerts, -1);
            for (int iVert = 0; (iVert) < (nVerts); ++iVert)
            {
                int neighborPairs[][/*2*/] = neighborPairss[iVert];
                int nNeighbors = neighborPairs.length;
                for (int iNeighbor = 0; (iNeighbor) < (nNeighbors); ++iNeighbor)
                {
                    int neighborPair[/*2*/] = neighborPairs[iNeighbor];
                    nextNeighbor[neighborPair[0]] = neighborPair[1];
                }
                int adj[] = adjs[iVert] = new int[nNeighbors];
                if (nNeighbors > 0)
                {
                    int neighbor = neighborPairs[0][0];
                    for (int iNeighbor = 0; (iNeighbor) < (nNeighbors); ++iNeighbor)
                    {
                        adj[iNeighbor] = neighbor;
                        int temp = nextNeighbor[neighbor];
                        nextNeighbor[neighbor] = -1;
                        neighbor = temp;
                    }
                    do { if (!(neighbor == neighborPairs[0][0])) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+1107 +"): " + "neighbor == neighborPairs[0][0]" + ""); } while (false); // full circle
                }
            }
        }

        System.out.println("    out computeAdjacencies");
        return adjs;
    } // computeAdjacencies


    public static Poly copyVerts(Poly p)
    {
        return new Poly((Object[])Arrays.copy(p.verts, Arrays.getDim(p.verts)),
                        p.inds,
                        p.aux,
                        p.repeatVectors);
    } // copyVerts

    /**
     * XXX and prune.  (should probably be part of the name).
     * Deep copies verts all the way down to scalars
     * (regardless of how deep verts is).
     */
    public static Poly separateAndCopyVerts(Poly p,
                                            int dimOfPartsToSeparate,
                                            boolean keepVertexOrder) // XXX I don't think this should ever be needed, but at the moment I'm tracking a bug that makes it make a difference XXX JAVADOC
    {
        Object newInds;
        {
            Object oldInds = p.inds;
            int nInds = java.lang.reflect.Array.getLength(oldInds);
            newInds = java.lang.reflect.Array.newInstance(oldInds.getClass().getComponentType(), nInds);
            copyInds(newInds, 0,
                     oldInds, 0,
                     nInds,
                     0);
        }

        Object oldVerts[] = p.verts;
        int nOld = oldVerts.length;
        int maxNew = Arrays.arrayLength(newInds, Arrays.getDim(newInds)); // worst case-- one vertex for every single index
        Object newVerts[] = (Object[])java.lang.reflect.Array.newInstance(oldVerts.getClass().getComponentType(), maxNew);
        int oldToNewVerts[] = VecMath.fillvec(nOld, -1); // one-to-many partial, but we only use it for one part at a time
        int oldVertsToMostRecentPart[] = VecMath.fillvec(nOld, -1);

        final int newToOldVerts[] = VecMath.fillvec(maxNew, -1); // many-to-one complete


        do { if (!(dimOfPartsToSeparate-1 >= 1)) throw new Error("Assumption failed at "+"com/donhatchsw/util/Poly.prejava"+"("+1155 +"): " + "dimOfPartsToSeparate-1 >= 1" + ""); } while (false); // XXX otherwise this clever trick doesn't work. however it might, if we make Arrays.flatten expand in appropriate cases. XXX I think I did that, need to think about this again
        Object temp = expandOrFlatten(newInds,dimOfPartsToSeparate+1);
        int parts[][][] = (int[][][])Arrays.flatten(temp,
                                                    1,
                                                    dimOfPartsToSeparate-1);
        int nParts = parts.length;


        if (false)
        {
            System.out.println("\"BEFORE separateAndCopyVerts:\"" + " = " + ("BEFORE separateAndCopyVerts:"));
            System.out.println("oldVerts.length" + " = " + (oldVerts.length));
            System.out.println("parts" + " =\n" + Arrays.toStringNonCompact(parts, "    ", "    "));
            System.out.println("newInds" + " =\n" + Arrays.toStringNonCompact(newInds, "    ", "    "));
        }

        int nNew = 0; // and counting
        for (int iPart = 0; (iPart) < (nParts); ++iPart)
        {
            int contoursThisPart[][] = parts[iPart];
            int nContoursThisPart = contoursThisPart.length;
            for (int iContourThisPart = 0; (iContourThisPart) < (nContoursThisPart); ++iContourThisPart)
            {
                int contour[] = contoursThisPart[iContourThisPart];
                int nVertsThisContour = contour.length;
                for (int iVertThisContour = 0; (iVertThisContour) < (nVertsThisContour); ++iVertThisContour)
                {
                    int iOld = contour[iVertThisContour];
                    int iNew;
                    if (oldVertsToMostRecentPart[iOld] < iPart)
                    {
                        iNew = nNew++;
                        newVerts[iNew] = Arrays.copy(oldVerts[iOld], Arrays.getDim(oldVerts[iOld]));
                        oldToNewVerts[iOld] = iNew;
                        newToOldVerts[iNew] = iOld;
                        oldVertsToMostRecentPart[iOld] = iPart;
                    }
                    else
                        iNew = oldToNewVerts[iOld];
                    contour[iVertThisContour] = iNew;
                }
            }
        }

        if (false)
        {
            System.out.println("\"AFTER separateAndCopyVerts:\"" + " = " + ("AFTER separateAndCopyVerts:"));
            System.out.println("oldVerts.length" + " = " + (oldVerts.length));
            System.out.println("parts" + " =\n" + Arrays.toStringNonCompact(parts, "    ", "    "));
            System.out.println("newInds" + " =\n" + Arrays.toStringNonCompact(newInds, "    ", "    "));
        }

        newVerts = (Object[])Arrays.subarray(newVerts, 0, nNew);

        if (keepVertexOrder)
        {
            final int newnewToNewVerts[] = VecMath.identityperm(nNew);
            SortStuff.sort(newnewToNewVerts,
                           new SortStuff.IntComparator() {
                               public int compare(int i, int j)
                               {
                                   if (newToOldVerts[i] < newToOldVerts[j])
                                       return -1;
                                   if (newToOldVerts[i] > newToOldVerts[j])
                                       return 1;
                                   if (i < j)
                                       return -1;
                                   if (i > j)
                                       return 1;
                                   return 0;
                               }
                           });
            int newToNewnewVerts[] = VecMath.invertperm(newnewToNewVerts);
            Object newnewVerts[] = (Object[])java.lang.reflect.Array.newInstance(oldVerts.getClass().getComponentType(), nNew);
            for (int iNew = 0; (iNew) < (nNew); ++iNew)
                newnewVerts[iNew] = newVerts[newnewToNewVerts[iNew]];

            newVerts = newnewVerts;
            //PRINT(nNew);
            //PRINTVEC(newnewToNewVerts);
            //PRINTVEC(newToNewnewVerts);
            //PRINTARRAY(newInds);
            redirectInds(newInds, newInds, newToNewnewVerts);
        }

        return new Poly(newVerts,
                        newInds,
                        p.aux,
                        p.repeatVectors);

    } // separateAndCopyVerts

    private static void redirectInds(Object outInds, Object inInds,
                                     int lut[]) // lookup table XXX JAVADOC
    {
        // result can be the same as inds,
        // but there must be no multiple instancing
        int n = java.lang.reflect.Array.getLength(outInds);
        do { if (!(n == java.lang.reflect.Array.getLength(inInds))) throw new Error("Assertion failed at "+"com/donhatchsw/util/Poly.prejava"+"("+1253 +"): " + "n == java.lang.reflect.Array.getLength(inInds)" + ""); } while (false);
        if (inInds instanceof int[])
        {
            int outInts[] = (int[])outInds;
            int inInts[] = (int[])inInds;
            for (int i = 0; (i) < (n); ++i)
                outInts[i] = lut[inInts[i]];
        }
        else
        {
            for (int i = 0; (i) < (n); ++i)
                redirectInds(java.lang.reflect.Array.get(outInds,i),
                             java.lang.reflect.Array.get(inInds,i),
                             lut);
        }
    } // redirectIndsInPlace

    public static Poly reverseContours(Poly p)
    {
        Object oldInds = p.inds;
        int indsDim = Arrays.getDim(oldInds);
        Object newInds = Arrays.copy(oldInds, indsDim); // complete copy
        int contours[][] = (int[][])Arrays.flatten(newInds, 0, indsDim-1);
        int nContours = contours.length;
        for (int iContour = 0; (iContour) < (nContours); ++iContour)
            Arrays.reverse(contours[iContour], contours[iContour]);
        return new Poly(p.verts,
                        newInds,
                        p.aux,
                        p.repeatVectors);
    } // reverseContours

    /** vert type must be double[] */
    public static Poly shrinkParts(Poly p,
                                   int dimOfPartsToShrink,
                                   double shrinkFactor,
                                   boolean keepVertexOrder)
    {
        if (shrinkFactor == 1.)
            return p;

        p = separateAndCopyVerts(p, dimOfPartsToShrink, keepVertexOrder);

        int parts[][] = (int[][])Arrays.flatten(p.getInds(dimOfPartsToShrink + 1),
                                                1, dimOfPartsToShrink);
        int nParts = parts.length;

        double verts[][] = (double[][])p.verts;
        int nVerts = verts.length;

        // so we don't do a vertex more than once...
        boolean done[] = new boolean[nVerts];
        VecMath.fillvec(done, false);

        // scratch for loop...
            double partCenter[] = (nVerts == 0 ? null : new double[verts[0].length]);

        for (int iPart = 0; (iPart) < (nParts); ++iPart)
        {
            int part[] = parts[iPart];
            int nVertsThisPart = part.length;
            if (nVertsThisPart > 0)
            {
                VecMath.averageIndexed(partCenter, part, verts);
                for (int iVertThisPart = 0; (iVertThisPart) < (nVertsThisPart); ++iVertThisPart)
                {
                    int iVert = part[iVertThisPart];
                    if (!done[iVert])
                    {
                        double vert[] = verts[part[iVertThisPart]];
                        VecMath.lerp(vert,
                                     partCenter,
                                     vert,
                                     shrinkFactor);
                        done[iVert] = true;
                    }
                }
            }
        }
        return p;
    } // shrinkParts


    public String toString()
    {
        String newline = System.getProperty("line.separator");
        String s = "";

        s += verts.length + " vert" + (verts.length == 1 ? "" : "s") + ":" + newline;
        s += Arrays.toStringNonCompact(verts, "    ", "    ");
        s += newline;

        s += "inds:" + newline;
        s += Arrays.toStringNonCompact(inds, "    ", "    ");
        s += newline;

        return s;
    } // toString



    /**
     * Prototype primitive arrays...
     * Adjs are vertex-to-neighbor-vertex lookups,
     * with the neighbors ordered clockwise around each vertex.
     * <p>
     * XXX I made some faces arrays public so apps
     *     don't need to go through the generic multidimensional array
     *     interface to access it... think about this.
     * <p>
     * XXX And now that verts are generic too, same for them.
     * <p>
     * XXX JAVADOC GROUP
     */
        public static final double tetraVerts[][] = {
            {-1,-1,-1},
            {1,1,-1},
            {1,-1,1},
            {-1,1,1},
        };
        public static final int tetraFaces[][][] = {
            {{1,3,2}}, {{0,2,3}}, {{0,3,1}}, {{0,1,2}},
        };
        private static final int tetraBackwardsFaces[][][] = {
            {{1,2,3}}, {{0,3,2}}, {{0,1,3}}, {{0,2,1}},
        };
        public static final int tetraAdjs[][] = {
            {1,3,2},{0,2,3},{0,3,1},{0,1,2},
        };

        private static final double cubeVerts[][] = {
            /* 0 */ {-1,-1,-1},
            /* 1 */ {1,-1,-1},
            /* 2 */ {-1,1,-1},
            /* 3 */ {1,1,-1},
            /* 4 */ {-1,-1,1},
            /* 5 */ {1,-1,1},
            /* 6 */ {-1,1,1},
            /* 7 */ {1,1,1},
        };
        private static final int cubeFaces[][][] = {
            {{0,2,3,1}},{{0,1,5,4}},{{0,4,6,2}},{{7,3,2,6}},{{7,6,4,5}},{{7,5,1,3}},
        };

        public static final int cubeAdjs[][] = {
            /* 0 */ {1,2,4},
            /* 1 */ {0,5,3},
            /* 2 */ {0,3,6},
            /* 3 */ {1,7,2},
            /* 4 */ {0,6,5},
            /* 5 */ {1,4,7},
            /* 6 */ {7,4,2},
            /* 7 */ {3,5,6},
        };

        private static final double gold = (Math.sqrt(5.)+1.)/2.;
        public static final double dodecaVerts[][] = {
            /* 0 */ {-1,-1,-1},
            /* 1 */ {1,-1,-1},
            /* 2 */ {-1,1,-1},
            /* 3 */ {1,1,-1},
            /* 4 */ {-1,-1,1},
            /* 5 */ {1,-1,1},
            /* 6 */ {-1,1,1},
            /* 7 */ {1,1,1},

            /* 8 */ {-1/gold,0,-gold},
            /* 9 */ {1/gold,0,-gold},
           /* 10 */ {-gold,-1/gold,0},
           /* 11 */ {-gold,1/gold,0},
           /* 12 */ {0,-gold,-1/gold},
           /* 13 */ {0,-gold,1/gold},

           /* 14 */ {-1/gold,0,gold},
           /* 15 */ {1/gold,0,gold},
           /* 16 */ {gold,-1/gold,0},
           /* 17 */ {gold,1/gold,0},
           /* 18 */ {0,gold,-1/gold},
           /* 19 */ {0,gold,1/gold},
        };
        public static final int dodecaFaces[][][] = {
            /* 0 */ {{0,8,9,1,12}},
            /* 1 */ {{0,10,11,2,8}},
            /* 2 */ {{0,12,13,4,10}},
            /* 3 */ {{1,16,5,13,12}},
            /* 4 */ {{1,9,3,17,16}},
            /* 5 */ {{2,18,3,9,8}},
            /* 6 */ {{2,11,6,19,18}},
            /* 7 */ {{4,13,5,15,14}},
            /* 8 */ {{4,14,6,11,10}},
            /* 9 */ {{7,15,5,16,17}},
           /* 10 */ {{7,17,3,18,19}},
           /* 11 */ {{7,19,6,14,15}},
        };
        public static final int dodecaAdjs[][] = {
            /* 0 */ {8,10,12},
            /* 1 */ {9,12,16},
            /* 2 */ {11,8,18},
            /* 3 */ {9,17,18},
            /* 4 */ {13,10,14},
            /* 5 */ {13,15,16},
            /* 6 */ {11,19,14},
            /* 7 */ {15,19,17},
            /* 8 */ {0,9,2},
            /* 9 */ {1,3,8},
           /* 10 */ {0,11,4},
           /* 11 */ {2,6,10},
           /* 12 */ {0,13,1},
           /* 13 */ {5,12,4},
           /* 14 */ {6,15,4},
           /* 15 */ {7,5,14},
           /* 16 */ {1,5,17},
           /* 17 */ {3,16,7},
           /* 18 */ {3,19,2},
           /* 19 */ {7,6,18},
        };
        // XXX should do some sanity checking to make sure I didn't get any
        // XXX inside out

        private static final double donutVerts[][] = {
            /* 0 */ {-3,-1,-3},
            /* 1 */ {3,-1,-3},
            /* 2 */ {-3,1,-3},
            /* 3 */ {3,1,-3},
            /* 4 */ {-3,-1,3},
            /* 5 */ {3,-1,3},
            /* 6 */ {-3,1,3},
            /* 7 */ {3,1,3},
            /* 8 */ {-1,-1,-1},
            /* 9 */ {1,-1,-1},
           /* 10 */ {-1,1,-1},
           /* 11 */ {1,1,-1},
           /* 12 */ {-1,-1,1},
           /* 13 */ {1,-1,1},
           /* 14 */ {-1,1,1},
           /* 15 */ {1,1,1},
        };
        private static final int donutFaces[][][] = {
            {{0,1,5,4},{8,12,13,9}}, // an unusual one first, for heuristic recognition
            {{0,2,3,1}},
            {{0,4,6,2}},
            {{7,3,2,6},{15,14,10,11}},
            {{7,6,4,5}},
            {{7,5,1,3}},
            {{15,11,9,13}},
            {{15,13,12,14}},
            {{8,9,11,10}},
            {{8,10,14,12}},
        };
        public static final int donutAdjs[][] = {
            /* 0 */ {1,2,4},
            /* 1 */ {0,5,3},
            /* 2 */ {0,3,6},
            /* 3 */ {1,7,2},
            /* 4 */ {0,6,5},
            /* 5 */ {1,4,7},
            /* 6 */ {7,4,2},
            /* 7 */ {3,5,6},
            /* 8 */ {12,10,9},
            /* 9 */ {11,13,8},
           /* 10 */ {14,11,8},
           /* 11 */ {10,15,9},
           /* 12 */ {13,14,8},
           /* 13 */ {15,12,9},
           /* 14 */ {10,12,15},
           /* 15 */ {14,13,11},
        };

        /** apex = origin, base = face 0 of dodecahedron */
        private static final double pentagonalPyramidVerts[][] = {
            {0,0,0},
            dodecaVerts[dodecaFaces[0][0][0]],
            dodecaVerts[dodecaFaces[0][0][1]],
            dodecaVerts[dodecaFaces[0][0][2]],
            dodecaVerts[dodecaFaces[0][0][3]],
            dodecaVerts[dodecaFaces[0][0][4]],
        };
        private static final int pentagonalPyramidFaces[][][] = {
            {{0,5,4}},
            {{0,4,3}},
            {{0,3,2}},
            {{0,2,1}},
            {{0,1,5}},
            {{1,2,3,4,5}},
        };
        public static final int pentagonalPyramidAdjs[][] = {
            /* 0 */ {1,2,3,4,5},
            /* 1 */ {2,0,5},
            /* 2 */ {3,0,1},
            /* 3 */ {4,0,2},
            /* 4 */ {5,0,3},
            /* 5 */ {1,0,4},
        };


    public static final Poly tetra = new Poly(tetraVerts, tetraFaces, null, null);
    public static final Poly tetraBackwards = new Poly(tetraVerts, tetraBackwardsFaces, null, null);
    public static final Poly cube = new Poly(cubeVerts, cubeFaces, null, null);
    public static final Poly dodeca = new Poly(dodecaVerts, dodecaFaces, null, null);
    public static final Poly pentagonalPyramid = new Poly(pentagonalPyramidVerts, pentagonalPyramidFaces, null, null);
    public static final Poly donut = new Poly(donutVerts, donutFaces, null, null);

    /**
     * Private static utilities...
     * XXX JAVADOC GROUP
     */
        /**
         * Add or remove the appropriate number
         * of highest-level dimensions so that the result
         * is of the deisred dimension.
         * <br>
         * XXX should this go into Arrays class?
         */
        private static Object expandOrFlatten(Object array, int desiredDim)
        {
            if (array == null)
                return null;

            //System.out.println("in expandOrFlatten (desiredDim="+desiredDim+")");
            //System.out.println("    before: array = "+Arrays.toString(array));


            int dim = Arrays.getDim(array);
            if (desiredDim < dim)
            {
                array = Arrays.flatten(array, 0, dim-desiredDim+1);
            }
            else
            {
                // XXX maybe this will be in flatten's semantics some day...
                // XXX however, the Integer thing is an application-specific
                // XXX assumption, so it might need another parameter in general.
                while (desiredDim > dim)
                {
                    // Expand into a singleton.
                    array = Arrays.singleton(array);
                    dim++;

                }
            }
            //System.out.println("    after: array = "+Arrays.toString(array));
            //System.out.println("out expandOrFlatten");
            return array;
        } // expandOrFlatten


        private static void transformVerts(double outVerts[][], int outStart,
                                           double inVerts[][],
                                           double rowMat[][])
        {
            int i, nVerts = inVerts.length;
            for (i = 0; (i) < (nVerts); ++i)
                outVerts[outStart+i] = VecMath.vxm(inVerts[i], rowMat);
        } // transformVerts

        /**
         * XXX public because I found it useful elsewhere...
         *     should it maybe be part of the Arrays class or something?
         */
        public static void copyInds(Object outInds, int outStart,
                                    Object inInds, int inStart,
                                    int n,
                                    int targetOffset)
        {
            do { if (!(inInds.getClass().getComponentType() != null)) throw new Error("Assumption failed at "+"com/donhatchsw/util/Poly.prejava"+"("+1617 +"): " + "inInds.getClass().getComponentType() != null" + ""); } while (false); // XXX can't handle simply Integer for now; might be straightforward but I haven't thought about it and it hasn't come up

            if (inInds instanceof int[])
            {
                int inInts[] = (int [])inInds;
                int outInts[] = (int [])outInds;
                for (int i = 0; (i) < (n); ++i)
                    outInts[outStart+i] = inInts[inStart+i] + targetOffset;
            }
            else // recurse
            {
                Object inObjs[] = (Object [])inInds;
                Object outObjs[] = (Object [])outInds;
                for (int i = 0; (i) < (n); ++i)
                {
                    Object inObj = inObjs[inStart+i];
                    int inObjLength = java.lang.reflect.Array.getLength(inObj);
                    Object outObj = java.lang.reflect.Array.newInstance(inObj.getClass().getComponentType(), inObjLength); // same type and length as array inObj
                    copyInds(outObj, 0,
                             inObj, 0,
                             inObjLength,
                             targetOffset);
                    outObjs[outStart+i] = outObj;
                }
            }
        } // copyInds

} // class Poly
