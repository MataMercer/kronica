
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

export type Content = {
  id: Number
  author: User
  createdAt: String
  updatedAt: String
  nsfw: Boolean
  tags: Tag[]
}

export type Article = Content & {
  title: string;
  body: string;
  attachments: FileModel[];
  characters: Character[];
  timeline: Timeline;
  youLiked: boolean;
  likeCount: number;
}

export type Character = Content & {
  name: string;
  body: string;
  attachments: FileModel[];
  profilePictures: FileModel[];
  traits: { value: string; name: string; }[];
}

export type FileModel = {
  id: number;
  name: string;
  caption: string;
  storageId: string;
}

export type Timeline = Content & {
  name: string;
  description: string;
}

export type Tag = {
  id: number;
  name: string;
  popularity: number;
}

export type Page<T> = {
  content: T[]
  pages: number
}
