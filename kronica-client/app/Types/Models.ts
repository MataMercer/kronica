

export type User = {
  id: number;
  name: string;
  role: keyof typeof UserRole;
};

//See the link for more information on how TypeScript enums work at compile time
//https://www.typescriptlang.org/docs/handbook/enums.html#enums-at-compile-time
export enum UserRole {
  UNAUTHENTICATED_USER = 0,
  BANNED_USER = 1,
  AUTHENTICATED_USER = 2,
  CONTRIBUTOR_USER = 3,
  ADMIN = 4,
  ROOT = 5
}

