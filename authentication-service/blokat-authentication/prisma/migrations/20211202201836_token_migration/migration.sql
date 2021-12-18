/*
  Warnings:

  - Added the required column `hashed_password` to the `users` table without a default value. This is not possible if the table is not empty.
  - Added the required column `salt` to the `users` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE "users" ADD COLUMN     "hashed_password" TEXT NOT NULL,
ADD COLUMN     "salt" TEXT NOT NULL;

-- CreateTable
CREATE TABLE "tokens" (
    "id" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "isUsed" BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT "tokens_pkey" PRIMARY KEY ("id")
);
