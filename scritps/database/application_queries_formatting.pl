# USAGE: perl application_queries_formatting.pl <SQL-FILE> <OUTPUT-FILE>
if (@ARGV != 2) {
  print "USAGE: perl application_queries_formatting.pl <SQL-FILE> <OUTPUT-FILE>\n\n";
  print "Format of the SQL query file should be:\n  | -- comment_that_will_end_up_in_output_using_#\n  | -- \$query_name\n  | ...\n  |  query\n  | ... ;\n\noutput for every query will be:\nquery_name=query-on-one-line\n\nNote 1: every query should end with a ';'\n";
  exit 1;
}

# format of the SQL query file should be:
#   | -- comment_that_will_end_up_in_output_using_#
#   | -- $query_name
#   | ...
#   |  query
#   | ... ;
#
# output for every query will be:
# query_name=query-on-one-line
#
# Note 1: every query should end with a ';'

open ($out, ">", $ARGV[1]);
pop @ARGV;

while (<>) {
  if (/^--.*?\$(.*)$/) {
    print $out "\n$1=";
  } elsif (/^-- (.*)$/) {
    print $out "\n\n# $1";
  } else {
    chomp $_;
    print $out $_;
  }
}
