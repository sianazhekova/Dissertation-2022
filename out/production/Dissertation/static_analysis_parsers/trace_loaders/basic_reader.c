#include <assert.h>
#include <stdio.h>
#include <stdlib.h>

/* Each mem_ref_t includes the type of reference (read or write),
 * the address referenced, and the size of the reference.
 */
typedef struct _mem_ref_t {
    char type;
    void *addr;
    size_t size;
    void *pc;
} mem_ref_t;

#define LOAD 0
#define STORE 1
#define START 2
#define END 4

/* Max number of mem_ref a buffer can have */
#define MAX_NUM_MEM_REFS 8192
/* The size of memory buffer for holding mem_refs. We read into it from the
 * file and then process each item one by one.
 */
#define MEM_BUF_SIZE (sizeof(mem_ref_t) * MAX_NUM_MEM_REFS)

#define LIM_LOOP_STARTS 2

int
main(int argc, char **argv)
{
    if (argc != 2) {
        fprintf(stderr, "Usage: %s trace\n", argv[0]);
        exit(1);
    }

    FILE *trace = fopen(argv[1], "r");
    assert(trace);

    mem_ref_t *mem_buf = (mem_ref_t *)malloc(MEM_BUF_SIZE);

    int read = 0;
    int entries = 0;
    int seen_end = 0;
    int first_loop_entries = 0;
    printf("NEW CHANGE");

    int seen_starts = 0;
    int seen_loop_ends = 0;
    while ((read = fread(mem_buf, sizeof(mem_ref_t), MAX_NUM_MEM_REFS, trace)) != 0) {
        for (int i = 0; i < read; ++i) {
            if (mem_buf[i].type == START) {
                seen_starts++;
                printf("Start: %p and seen starts %d\n", mem_buf[i].pc, seen_starts);

            } else if (mem_buf[i].type == END) {
                seen_loop_ends++;
                
                /*if (seen_loop_ends > seen_starts) {
                    seen_loop_ends--;
                } */
                printf("End:   %p  %d\n", mem_buf[i].pc, seen_loop_ends);
                if (!seen_end) {
                    seen_end = 1;
                    first_loop_entries = entries + 1;
                }
                if (seen_loop_ends >= 5 && seen_starts == 2) {
                    fclose(trace);
                    printf("Total %d entries\n", entries);
                    printf("First invocation complete after %d entries (%zu bytes)\n",
                        first_loop_entries, first_loop_entries * sizeof(mem_ref_t)); 
                    return 0;
                }
            } else {
                printf("%s %p %p %zu bytes\n", mem_buf[i].type ? "Store:" : "Load: ",
                       mem_buf[i].pc, mem_buf[i].addr, mem_buf[i].size);
            }
            entries++;
        }
    }

    fclose(trace);
    printf("Total %d entries\n", entries);
    printf("First invocation complete after %d entries (%zu bytes)\n",
           first_loop_entries, first_loop_entries * sizeof(mem_ref_t)); 
    return 0;
}