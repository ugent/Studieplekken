import { PrismaClient } from "@prisma/client";

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
