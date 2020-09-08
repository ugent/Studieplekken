# USAGE: perl application_queries_formatting.pl <SQL-FILE> <COLUMNS-FILE>
#
# format of the SQL-FILE should be:
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
# Note 2: comments in queries (between '/*' and '*/') are allowed,
#   but make sure that there are no constructions like: "... */ ... /* ..."
#   on the same line.
#   However, constructions like: "... /* ... */ ..." will be handled correctly
#
# The COLUMNS-FILE will just be copied into the output.

# Check for correct usage
if (@ARGV != 2) {
  print "USAGE: perl application_queries_formatting.pl <SQL-FILE> <COLUMNS-FILE>\n\n";
  print "Format of the SQL-FILE should be:\n  | -- comment_that_will_end_up_in_output_using_#\n  | -- \$query_name\n  | ...\n  |  query\n  | ... ;\n\noutput for every query will be:\nquery_name=query-on-one-line\n\nNote 1: every query should end with a ';'\nNote 2: comments in queries (between '/*' and '*/') are allowed,\n    but make sure that there are no constructions like: \"... */ ... /* ...\"\n    on the same line.\n    However, constructions like: \"... /* ... */ ...\" will be handled correctly\n\nThe COLUMNS-FILE will just be copied into the output.";
  exit 1;
}

my $is_in_comment = 0;
my $is_in_query = 0;
my $query = "";

# Map the formatted file to a one-query-per-line format
open (my $sql_file, "<", $ARGV[0]) || die "Can't open sql file file '$ARGV[0]'";

# Skip the file explanatory comment
while (<$sql_file>) {
  if (!/^--.*$/) {
    last;
  }
}

while (<$sql_file>) {
  chomp; # remove trailing \n
  $_ =~ s/^\s+//; # remove leading whitespaces
  $_ =~ s/\s+$//; # remove trailing whitespaces

  # ... /* ... -> $is_in_comment = 1
  if (/^.*\/\*.*$/) {
    $is_in_comment = 1;
  }

  # ... */ ... -> $is_in_comment = 0
  if (/^.*\*\/.*$/) {
    $is_in_comment = 0;
  }

  if (/^-- \$(.*)$/) {
    # -- $name_of_query
    print "$1=";
    $is_in_query = 1;
  } elsif (/^-- (.*)$/) {
    # -- comment
    print "# $1\n";
  } elsif (/^$/) {
    # empty line
    print "\n" unless $is_in_comment == 1 || $is_in_query == 1;
  } elsif (!/^\s+$/) {
    if ($query =~ /^\s*$/) {
      $query = $_;
    } else {
      $query = "$query $_";
    }

    if (/^.*;$/) {
      print "$query";
      $query = "";
      $is_in_query = 0;
    }
  }
}

print "\n\n";

# Print content of COLUMNS-FILE
open (my $columns_file, "<", $ARGV[1]) || die "Can't open sql file file '$ARGV[1]'";
while (<$columns_file>) {
  print $_;
}
