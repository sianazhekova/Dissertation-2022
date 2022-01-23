package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/* A wrapper class of the map representing the Pending, History Point Table */

public class PointTable implements Table, Cloneable {

    public Map<BigInteger, List<TableEntryPC>> table;

    public PointTable() {
        this.table = new HashMap<>();
    }

    public PointTable(Map<BigInteger, List<TableEntryPC>> inputTable) { this.table = inputTable; }

    public List<TableEntryPC> getTableEntry(BigInteger key) {
        return table.get(key); // cloneListEntry(key);
    }

    public List<TableEntryPC> getOrDefault(BigInteger key, List<TableEntryPC> newList) {
        if (table.containsKey(key)) return table.get(key); //cloneListEntry(key);

        return newList;
    }

    public Set<BigInteger> getKeySet() {
        return (Set<BigInteger>) table.keySet();
    }

    public boolean containsKey(BigInteger key) {
        return table.containsKey(key);
    }

    public void put(BigInteger keyAddr, List<TableEntryPC> list) {
        table.put(keyAddr, list);
    }

    public void addNewEntryForAddress(BigInteger memAddr, BigInteger PCAddr, MemoryAccess accessMode, long tripCount) {
        List<TableEntryPC> listToRet = table.getOrDefault(memAddr, new LinkedList<>());
        int sizeList = listToRet.size();
        if (sizeList > 0) {
            TableEntryPC tailEntry = listToRet.get(sizeList - 1);
            if (tailEntry.getAddressPC().equals(PCAddr) && tailEntry.getMemAccessType() != MemoryAccess.WRITE && tailEntry.getMemAccessType() == accessMode) {
                tailEntry.setTripCount(tripCount);
                tailEntry.setNumOccurrence(tailEntry.getNumOccurrence() + 1);
                return;
            }
        }
        TableEntryPC newEntry = new TableEntryPC(PCAddr, tripCount, accessMode, 1);
        listToRet.add(newEntry);
        table.put(memAddr, listToRet);
    }

    public boolean containsPCKey(BigInteger key, BigInteger PCAddress) {
        if (table.containsKey(key)) {
            List<TableEntryPC> listPCs = table.get(key);
            ListIterator<TableEntryPC> iteratorPCs = listPCs.listIterator();
            TableEntryPC currBlock = null;
            while (iteratorPCs.hasNext()) {
                currBlock = iteratorPCs.next();
                BigInteger currPC = currBlock.getAddressPC();
                if (currPC.equals(PCAddress));
                    return true;
            }
        }
        return false;
    }

    public TableEntryPC returnEntryForKey(BigInteger key, BigInteger PCAddress) {
        if (table.containsKey(key)) {
            List<TableEntryPC> listPCs = table.get(key);
            ListIterator<TableEntryPC> iteratorPCs = listPCs.listIterator();
            TableEntryPC currBlock = null;
            while (iteratorPCs.hasNext()) {
                currBlock = iteratorPCs.next();
                BigInteger currPC = currBlock.getAddressPC();
                if (currPC.equals(PCAddress))
                    return currBlock;
            }
        }
        return null;
    }

    public boolean deletePC(BigInteger key, BigInteger PCAddress) {
        if (table.containsKey(key)) {
            List<TableEntryPC> listPCs = table.get(key);
            Iterator<TableEntryPC> iteratorPCs = listPCs.iterator();
            TableEntryPC currBlock = null;
            while (iteratorPCs.hasNext()) {
                currBlock = iteratorPCs.next();
                BigInteger currPC = currBlock.getAddressPC();
                if (currPC.equals(PCAddress)) {
                    iteratorPCs.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAccessType(BigInteger memRefKey, MemoryAccess readOrWrite) {
        if (table.containsKey(memRefKey)) {
            List<TableEntryPC> listPCs = table.get(memRefKey);
            //assert(listPCs!=null);
            Iterator<TableEntryPC> iteratorPCs = listPCs.iterator();
            TableEntryPC currEntry = null;
            while (iteratorPCs.hasNext()) {
                currEntry = iteratorPCs.next();
                MemoryAccess currAccType = currEntry.getMemAccessType();
                if (readOrWrite.equals(currAccType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mergeWith(@NotNull PointTable otherTable) {
        for (BigInteger keyAddr : otherTable.getKeySet()) {
            if (!table.containsKey(keyAddr)) {
                table.put(keyAddr, otherTable.getTableEntry(keyAddr));     //cloneListEntry(keyAddr));
            } else {
                List<TableEntryPC> listEntry = table.get(keyAddr);
                listEntry.addAll(otherTable.getTableEntry(keyAddr));     //cloneListEntry(keyAddr));
            }
        }
    }

    public void mergeAddressLine(BigInteger refAddress, List<TableEntryPC> listToMerge) {
        List<TableEntryPC> updatedList = this.table.getOrDefault(refAddress, new LinkedList<>());
        updatedList.addAll(listToMerge);
        this.table.put(refAddress, updatedList);
    }

    public void clear() {
        // Explicitly clearing the entries of the hash-table, that represents a mapping between the referenced memory addresses
        // and the lists of Table Entry blocks

        /*for (BigInteger key : table.keySet()) {
            table.get(key).clear();
        }*/
        table.clear();
    }

    public boolean isPointTableEmpty() { return table.isEmpty(); }

    public List<TableEntryPC> cloneListEntry(BigInteger keyAddress) {
        if (!table.containsKey(keyAddress)) return null;

        List<TableEntryPC> inList = table.get(keyAddress);
        List<TableEntryPC> copiedList = inList.stream()
                .map(e -> new TableEntryPC(e.getAddressPC(), e.getTripCount(), e.getMemAccessType(), e.getNumOccurrence()))
                .collect(Collectors.toList());

        return copiedList;
    }

    @Override
    public PointTable clone() {
    Map<BigInteger, List<TableEntryPC>> copiedMap = new HashMap<>();
        for (Map.Entry<BigInteger, List<TableEntryPC>> currEntry : table.entrySet()) {
            BigInteger addrKey = currEntry.getKey();
            List<TableEntryPC> listEntry = currEntry.getValue();
            List<TableEntryPC> copiedList = listEntry.stream()
                    .map(entry -> new TableEntryPC(entry.getAddressPC(), entry.getTripCount(), entry.getMemAccessType(), entry.getNumOccurrence()))
                    .collect(Collectors.toList());
            copiedMap.put(addrKey, copiedList);
        }
        return new PointTable(copiedMap);
    }

}
