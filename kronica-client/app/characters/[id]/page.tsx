import { fetchCharacter } from "@/app/fetch/characters";

export default async function CharacterPage({
    params,
}: {
    params: { id: string };
}) {
    const id = params.id;
    const character = id && (await fetchCharacter(id));
    console.log(character);

    return (
        character && (
            <div className="">
                <h1 className="text-3xl flex justify-center">
                    {character.name}
                </h1>
                <div className="m-2  p-2 border-black border-2">
                    <div className="border-black border-2 flex justify-center text-2xl font-bold">
                        {character.name}
                    </div>
                    <div className="">
                        <DataRow infoKey="Name" infoValue={character.name} />
                        <DataRow
                            infoKey="Status"
                            infoValue={character.status}
                        />
                        <DataRow
                            infoKey="First Seen"
                            infoValue={character.gender}
                        />
                        <DataRow
                            infoKey="Age"
                            infoValue={character.age.toString()}
                        />
                        <DataRow
                            infoKey="Birthday"
                            infoValue={character.birthday}
                        />
                        <DataRow
                            infoKey="First Seen"
                            infoValue={character.firstSeen}
                        />
                        {character.traits &&
                            Object.entries(character.traits).map(
                                (it, index) => (
                                    <DataRow
                                        key={index}
                                        infoKey={it[0]}
                                        infoValue={it[1] as string}
                                    />
                                )
                            )}
                    </div>
                </div>
                <div className="m-2">{character.body}</div>
            </div>
        )
    );
}

type DataRowProps = {
    infoKey: string;
    infoValue: string;
};

function DataRow({ infoKey, infoValue }: DataRowProps) {
    return (
        infoValue && (
            <div className="grid grid-cols-2">
                <div className="font-bold">{infoKey}</div>
                <div className="">{infoValue}</div>
            </div>
        )
    );
}
