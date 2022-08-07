/*
  Warnings:

  - Added the required column `email` to the `tokens` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE "tokens" ADD COLUMN     "email" VARCHAR(255);
