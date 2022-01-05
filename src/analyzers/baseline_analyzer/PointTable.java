package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/* A wrapper class of the map representing the Pending, History Point Table */

public class PointTable implements Table, Cloneable {

    Map<Long, List<TableEntryPC>> table;

    public PointTable() {
        this.table = new HashMap<>();
    }

    public PointTable(Map<Long, List<TableEntryPC>> inputTable) { this.table = inputTable; }

    public List<TableEntryPC> getTableEntry(Long key) {
        return cloneListEntry(key);
    }

    public List<TableEntryPC> getOrDefault(Long key, List<TableEntryPC> newList) {
        if (table.containsKey(key)) return cloneListEntry(key);

        return newList;
    }

    public Set<Long> getKeySet() {
        return (Set<Long>) table.keySet();
    }

    public boolean containsKey(Long key) {
        return table.containsKey(key);
    }

    public void put(Long keyAddr, List<TableEntryPC> list) {
        table.put(keyAddr, list);
    }

    public void addNewEntryForAddress(long memAddr, long PCAddr, MemoryAccess accessMode, long tripCount) {
        List<TableEntryPC> listToRet = table.getOrDefault(memAddr, new LinkedList<>());
        int sizeList = listToRet.size();
        if (sizeList > 0) {
            TableEntryPC tailEntry = listToRet.get(sizeList - 1);
            if (tailEntry.getAddressPC() == PCAddr && tailEntry.getMemAccessType() == accessMode) {
                tailEntry.setTripCount(tripCount);
                tailEntry.setNumOccurrence(tailEntry.getNumOccurrence() + 1);
                return;
            }
        }
        TableEntryPC newEntry = new TableEntryPC(PCAddr, tripCount, accessMode, 1);
        listToRet.add(newEntry);
        table.put(memAddr, listToRet);
    }

    public boolean containsPCKey(Long key, Long PCAddress) {
        if (table.containsKey(key)) {
            List<TableEntryPC> listPCs = table.get(key);
            ListIterator<TableEntryPC> iteratorPCs = listPCs.listIterator();
            TableEntryPC currBlock = null;
            while (iteratorPCs.hasNext()) {
                currBlock = iteratorPCs.next();
                Long currPC = currBlock.getAddressPC();
                if (currPC == PCAddress)
                    return true;
            }
        }
        return false;
    }

    public TableEntryPC returnEntryForKey(Long key, Long PCAddress) {
        if (table.containsKey(key)) {
            List<TableEntryPC> listPCs = table.get(key);
            ListIterator<TableEntryPC> iteratorPCs = listPCs.listIterator();
            TableEntryPC currBlock = null;
            while (iteratorPCs.hasNext()) {
                currBlock = iteratorPCs.next();
                Long currPC = currBlock.getAddressPC();
                if (currPC == PCAddress)
                    return currBlock;
            }
        }
        return null;
    }

    public boolean deletePC(Long key, Long PCAddress) {
        if (table.containsKey(key)) {
            List<TableEntryPC> listPCs = table.get(key);
            Iterator<TableEntryPC> iteratorPCs = listPCs.iterator();
            TableEntryPC currBlock = null;
            while (iteratorPCs.hasNext()) {
                currBlock = iteratorPCs.next();
                Long currPC = currBlock.getAddressPC();
                if (currPC == PCAddress) {
                    iteratorPCs.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAccessType(Long memRefKey, MemoryAccess readOrWrite) {
        if (table.containsKey(memRefKey)) {
            List<TableEntryPC> listPCs = table.get(memRefKey);
            //assert(listPCs!=null);
            Iterator<TableEntryPC> iteratorPCs = listPCs.iterator();
            TableEntryPC currEntry = null;
            while (iteratorPCs.hasNext()) {
                currEntry = iteratorPCs.next();
                MemoryAccess currAccType = currEntry.getMemAccessType();
                if (readOrWrite == currAccType) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mergeWith(@NotNull PointTable otherTable) {
        for (Long keyAddr : otherTable.getKeySet()) {
            if (!table.containsKey(keyAddr)) {
                table.put(keyAddr, otherTable.cloneListEntry(keyAddr));
            } else {
                List<TableEntryPC> listEntry = table.get(keyAddr);
                listEntry.addAll(otherTable.cloneListEntry(keyAddr));
            }
        }
    }

    public void mergeAddressLine(Long refAddress, List<TableEntryPC> listToMerge) {
        if (!this.table.containsKey(refAddress)) {

        }


    }

    public void accNewKilledBits(Long keyAddress, Set<Long> writes) {


    }

    public void clear() {
        // Explicitly clearing the entries of the hash-table, that represents a mapping between the referenced memory addresses
        // and the lists of Table Entry blocks
        for (Long key : table.keySet()) {
            table.get(key).clear();
        }
        table.clear();
    }

    public List<TableEntryPC> cloneListEntry(Long keyAddress) {
        if (!table.containsKey(keyAddress)) return null;

        List<TableEntryPC> inList = table.get(keyAddress);
        List<TableEntryPC> copiedList = inList.stream()
                .map(e -> new TableEntryPC(e.getAddressPC(), e.getTripCount(), e.getMemAccessType(), e.getNumOccurrence()))
                .collect(Collectors.toList());

        return copiedList;
    }

    @Override
    public PointTable clone() {
        Map<Long, List<TableEntryPC>> copiedMap = new HashMap<>();
        for (Map.Entry<Long, List<TableEntryPC>> currEntry : table.entrySet()) {
            Long addrKey = currEntry.getKey();
            List<TableEntryPC> listEntry = currEntry.getValue();
            List<TableEntryPC> copiedList = listEntry.stream()
                    .map(entry -> new TableEntryPC(entry.getAddressPC(), entry.getTripCount(), entry.getMemAccessType(), entry.getNumOccurrence()))
                    .collect(Collectors.toList());
            copiedMap.put(addrKey, copiedList);
        }
        return new PointTable(copiedMap);
    }

}
