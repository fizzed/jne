#include <stdio.h>
#include <string.h>
#include <errno.h>

void cat_it(FILE *fp)
{
  char buf[BUFSIZ];

  while (fgets(buf, sizeof(buf), fp)) {
    fputs(buf, stdout);
  }
}

int main(int argc, char **argv)
{
  if (argc < 2) {
    cat_it(stdin);
  } else {
    int i;
    FILE *fp;
    for(i = 1;argv[i];++i)
    {
      if (!(fp = fopen(argv[i], "r"))) {
        fprintf(stderr, "jcat: %s: %s\n", argv[i], strerror(errno));
        return 1;
      } else {
        cat_it(fp);
        fclose(fp);
      }
    }
  }
  return 0;
}
