/**
 * Is 'dateStr' in format 'YYYY-MM-DD'?
 * Is 'dateStr' a valid date?
 */
export function isStringValidDateForDB(dateStr: string): boolean {
  const regexp = new RegExp('^[0-9]{4}-[0-9]{2}-[0-9]{2}$');
  if (!regexp.test(dateStr)) {
    return false;
  }

  const date = new Date(dateStr);
  return !isNaN(date.getTime());
}

/**
 * Is 'timeStr' in format 'HH:MI'?
 */
export function isStringValidTimeForDBWithoutSeconds(timeStr: string): boolean {
  const regexp = new RegExp('^[0-9]{2}:[0-9]{2}$');
  return regexp.test(timeStr);
}

/**
 * Is 'dateTimeStr' in format 'YYYY-MM-DD HH:MI'?
 * Is 'dateTimeStr' a valid date?
 */
export function isStringValidDateTimeForDB(dateTimeStr: string): boolean {
  const regexp = new RegExp('^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}$');
  if (!regexp.test(dateTimeStr)) {
    return false;
  }

  const date = new Date(dateTimeStr);
  return !isNaN(date.getTime());
}
