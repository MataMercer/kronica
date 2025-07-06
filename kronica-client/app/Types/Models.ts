
export type User = {
  id: number;
  name: string;
  role: keyof typeof UserRole;
};

export type Character = {
  id: number;
  name: string;
  body: string;
  author: User;
  attachments: FileModel[];
  profilePictures: FileModel[];
  traits: {value: string; name: string;}[];
}

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

export type Article = {
  id: number;
  title: string;
  body: string;
  author: User;
  attachments: FileModel[];
  characters: Character[];
  timeline: Timeline;
  youLiked: boolean;
  likeCount: number;
}
export type FileModel = {
  id: number;
  name: string;
  caption: string;
  storageId: string;
}

export type Timeline = {
  id: number;
  name: string;
  description: string;
  author: User;
}
