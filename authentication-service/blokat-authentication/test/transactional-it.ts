import { PrismaClient } from "@prisma/client";

/** Wrapper around the classic test declaration, to run the test in a transaction.
 *
 * This is necessary for tests which run on the database. 
 * Such tests should only be used judiciously, for integration tests, as they are slow.
 **/
export const transactionalIt = (
  prisma: PrismaClient,
  test_name: string,
  test: () => Promise<unknown>,
) =>
  async function () {
    await prisma.$transaction(async (prisma) => {
      await prisma.$executeRaw`BEGIN`;
      const itWrap = it(test_name, test);
      await prisma.$executeRaw`ROLLBACK`;
      return itWrap;
    });
  };
