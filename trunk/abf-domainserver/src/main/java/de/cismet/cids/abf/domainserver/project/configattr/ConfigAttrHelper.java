/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConfigAttrHelper {

    //~ Static fields/initializers ---------------------------------------------

    public static final String LEAF_GROUP = "__config_attr_entry_leaf__"; // NOI18N
    public static final String ENTRY_SPLITTER = "\\.";                    // NOI18N
    public static final char ENTRY_SPLITTER_CHAR = '.';

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigAttrHelper object.
     */
    private ConfigAttrHelper() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   entries  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getSimilarity(final List<ConfigAttrEntry> entries) {
        if ((entries == null) || entries.isEmpty()) {
            return null;
        }

        String similarity = entries.get(0).getKey().getKey();
        for (final ConfigAttrEntry entry : entries) {
            similarity = getSimilarity(similarity, entry.getKey().getKey());
        }

        return similarity;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   entries  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getLastSimilarity(final List<ConfigAttrEntry> entries) {
        final String[] split = ConfigAttrHelper.normalisedSplit(ConfigAttrHelper.getSimilarity(entries),
                ConfigAttrHelper.ENTRY_SPLITTER);
        if (split.length == 0) {
            return entries.get(0).getKey().getKey();
        } else {
            return split[split.length - 1];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s1  DOCUMENT ME!
     * @param   s2  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getSimilarity(final String s1, final String s2) {
        final StringBuilder sb = new StringBuilder();

        final String[] tokens1 = normalisedSplit(s1, ENTRY_SPLITTER);
        final String[] tokens2 = normalisedSplit(s2, ENTRY_SPLITTER);

        for (int i = 0; (i < tokens1.length) && (i < tokens2.length); ++i) {
            if (tokens1[i].equals(tokens2[i])) {
                sb.append(tokens1[i]).append(ENTRY_SPLITTER_CHAR);
            }
        }

        // delete the last dot
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   toSplit   DOCUMENT ME!
     * @param   splitter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String[] normalisedSplit(final String toSplit, final String splitter) {
        final String[] normalSplit = toSplit.split(splitter);
        final ArrayList<String> normalisedSplit = new ArrayList<String>(normalSplit.length);
        for (final String token : normalSplit) {
            if (!token.isEmpty()) {
                normalisedSplit.add(token);
            }
        }

        return normalisedSplit.toArray(new String[normalisedSplit.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   entries  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Map<String, List<ConfigAttrEntry>> groupSimilars(final List<ConfigAttrEntry> entries) {
        final Map<String, List<ConfigAttrEntry>> groups = new LinkedHashMap<String, List<ConfigAttrEntry>>();
        String similarity = getSimilarity(entries);
        if (similarity == null) {
            similarity = ""; // NOI18N
        }

        final int similarParts = normalisedSplit(similarity, ENTRY_SPLITTER).length;

        for (final ConfigAttrEntry entry : entries) {
            final String key = entry.getKey().getKey();
            final String[] tokens = normalisedSplit(key, ENTRY_SPLITTER);
            final String group;
            if (tokens.length <= similarParts) {
                group = LEAF_GROUP;
            } else {
                group = tokens[similarParts];
            }

            if (!groups.containsKey(group)) {
                groups.put(group, new ArrayList<ConfigAttrEntry>());
            }

            groups.get(group).add(entry);
        }

        return groups;
    }
}
