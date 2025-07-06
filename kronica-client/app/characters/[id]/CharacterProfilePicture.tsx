"use client";
import clsx from "clsx";
import Image from "next/image";
import { it } from "node:test";
import { useState } from "react";
import {FileModel} from "@/app/Types/Models";

type CharacterProfilePictureProps = {
    profilePictures: FileModel[];
};

export default function CharacterProfilePicture({
    profilePictures,
}: CharacterProfilePictureProps) {
    const [currentIndex, setCurrentIndex] = useState(0);

    const profilePicture = profilePictures[currentIndex];

    return (
        <>
            {/* {profilePictures.map((it) => (
                <div
                    className="flex flex-col justify-center border-black border-2 m-2"
                    key={it.id}
                >
                    <span className="text-center">{it.caption}</span>
                    <Image
                        width={200}
                        height={200}
                        src={`http://localhost:7070/api/files/serve/${it.storageId}/${it.name}`}
                        alt=""
                    />
                </div>
            ))} */}
            {
                <div className="bg-black flex flex-col justify-center items-center">
                    <div>
                        {profilePictures.map((it, index) => (
                            <button
                                className={`
                                m-1 px-2  text-black capitalize border-white border-2 
                                ${
                                    currentIndex == index
                                        ? " bg-black text-white"
                                        : "bg-white"
                                }
                            )`}
                                key={it.id}
                                value={index}
                                onClick={(e) => {
                                    setCurrentIndex(e.target.value);
                                }}
                            >
                                {it.caption || `Picture ${index + 1}`}
                            </button>
                        ))}
                    </div>
                    {profilePictures.length > 0 && (
                        <Image
                            width={200}
                            height={200}
                            src={`http://localhost:7070/api/files/serve/${profilePicture.storageId}/${profilePicture.name}`}
                            alt=""
                            className="m-1"
                        />
                    )}
                </div>
            }
        </>
    );
}
